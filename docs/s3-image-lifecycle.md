# 리뷰 이미지의 S3 Lifecycle

## 1. 기준과 실제 기존 흐름

분석 기준은 `refactor/payment-architecture`의 `62410ae`이다. 새 작업 브랜치는
`refactor/s3-lifecycle-cleanup`이며 원격 fetch 후 두 브랜치의 기준 커밋이 같음을 확인했다.
이번 변경은 Public → Private 전환이나 Presigned URL 신규 도입이 아니다.
Bucket의 실제 ACL, 정책, 버전 관리 및 기존 Lifecycle은 이 저장소만으로 검증할 수 없다.

| 항목 | 실제 코드 |
|---|---|
| 설정 | `config/S3Config`: SDK v2 `S3Client`, `S3Presigner`; SDK v1 `AmazonS3Client` Bean도 존재하지만 사용처 없음 |
| PUT API | `utils/s3/S3Controller`, `GET /s3/presigned/put?category=review&fileName=photo.webp` |
| 기존 키 생성 | URL 발급 시 `S3Service.createFileName`: `category/원본파일명_UUID.확장자` |
| PUT 서명 | 3분, `Content-Type: image/webp`; URL 문자열을 `BaseResponse.result`로 반환 |
| 발급 시 DB/S3 | DB 행 생성도 실제 S3 업로드도 하지 않음. 클라이언트가 URL로 PUT |
| 리뷰 등록 | `ReviewServiceImpl.createReview`: 상담 조회 → Review 저장 → 요청 `imageUrls`를 `ReviewImage`로 저장 |
| 이미지 연결 | `ReviewMapper.toReviewImage`가 생성 시부터 Review를 지정함. 별도 임시 이미지 생성/연결 API 없음 |
| 리뷰 수정 | 현재 이미지 중 유지하지 않을 행을 제거하고 추가 키로 새 행 생성; 기존에는 S3 삭제가 DB 커밋 전 실행됨 |
| GET 서명 | `S3Service.generatePresignedGetUrl`, 10분; `/s3/presigned/get?fileUrl=ObjectKey`, 빈 키는 null |
| DB 저장값 | 필드명은 `url`, DTO는 `imageUrls`지만 실제 의도/조회 코드는 S3 Object Key. 기존 입력 검증 부재로 URL 등 잘못된 값이 저장될 가능성은 있음 |
| 조회 도메인 | `ReviewMapper`와 `PropertyMapper`가 저장 키로 GET URL 생성 |
| 다른 이미지 도메인 | `AdminPropertyServiceImpl` 매물 이미지 등록/수정. 상담사 `CounselorImage` 엔티티/Repository/Mapper가 있으나 서비스의 실제 이미지 저장·수정 호출은 주석 처리됨 |

**S3 객체만 있고 ReviewImage 행은 없는 상태가 가능하다.** URL 발급과 PUT에는 DB 쓰기가 없으므로
업로드 이후 사용자가 취소하거나 앱이 종료되면 그대로 남는다. 리뷰 저장 트랜잭션이 실패해도 같은 상태가 된다.
현재 실행 가능한 리뷰 등록 경로는 `review IS NULL`인 임시 행을 먼저 만들지 않는다.
스케줄러가 대상으로 삼는 행은 과거 데이터, 별도 운영 입력 등으로 존재할 수 있지만 그 생성 경로를
현재 API에서 확인한 것은 아니다.

## 2. 기존 스케줄러의 정확한 역할

`batch/review/ReviewImageCleanupScheduler`는 Spring Batch Job이 아니라 `@Scheduled` 컴포넌트이다.

- cron: `0 */10 * * * *` — 10분마다 실행, 별도 timezone 지정 없음.
- `LocalDateTime.now().minusMinutes(30)` — 30분은 코드에 직접 지정됨.
- `ReviewImageRepository.findUnusedImagesBefore`: `review is null AND createdAt < :cutoff`.
- 대상별 S3 HEAD → 존재하면 DELETE → DB 행 delete. S3 객체가 없으면 DB 행만 삭제.
- S3 오류가 발생하면 catch/log 후 그 행의 DB delete는 실행하지 않으므로 다음 실행에서 재시도 가능.
- S3 삭제 이후 DB 삭제/커밋이 실패하면 객체는 복구되지 않는다. 다음 실행에서는 HEAD 404 후 DB 삭제를 시도할 수 있다.
- 전체 메서드가 하나의 DB 트랜잭션이다. JPA 오류로 rollback-only가 되거나 flush/commit 시 실패하는 경우
  행별 catch만으로 DB 작업이 성공한다고 보장할 수 없다.
- S3 목록 조회나 전체 DB 참조 대조는 없다. 매물·상담사 이미지, DB 없는 객체, 연결된 행의 잘못된 참조는 정리하지 못한다.
- 리뷰 전체 삭제 시 이미지 DB 행은 cascade/orphanRemoval로 삭제되지만 S3 삭제는 기존에도 없었다.

기존 S3 전용 테스트/스케줄러 테스트는 없었다. 기존 테스트에는 애플리케이션 컨텍스트,
매물 조회, 결제·상담 등의 테스트가 있다. 상담 알림에 쓰이는 Spring Batch와 관련 의존성은 별도 기능이므로 유지한다.

## 3. 대안 비교

아래 호출 수는 클라이언트 PUT 1회를 제외한 일반적인 이미지 1개 기준이다.

| 기준 | 기존 스케줄러 유지 | 임시 prefix + Lifecycle | 임시 tag + Lifecycle |
|---|---|---|---|
| 구현/변경 범위 | 작음, 현재 DB 조회/삭제 유지 | 키 규칙·복사·도메인 저장 연결·검증·운영 규칙 | PUT tag 서명/클라이언트 헤더와 tag 전환·검증·운영 규칙 |
| S3 호출 | 정리 대상마다 HEAD + DELETE | 최초 확정 HEAD + COPY, 재시도 HEAD; temp 삭제는 Lifecycle | 확정 때 보통 GetObjectTagging + PutObjectTagging; 검증 방식에 따라 HEAD 추가 |
| DB 요청 미도달 | 객체를 찾지 못함 | 임시 prefix 객체 자동 만료 | 업로드 시 tag가 확실히 부여되면 자동 만료 |
| DB 저장 실패 | DB 행이 없으면 누락 | temp 원본은 남음. 선복사 후 롤백이면 영구 사본 누수 가능 | 선전환 후 롤백이면 active 객체 누수 가능 |
| DB 커밋 후 확정 실패 | 해당 단계 없음 | 이 순서를 사용하지 않음 | 커밋 후 tag 변경은 정상 이미지 만료 위험 |
| 정상 이미지 보호 | DB 후보 조건·동시성에 의존 | DB는 temp와 물리적으로 다른 키만 참조 | 동일 키의 tag 상태·만료 대기·재업로드에 의존 |
| 재시도/멱등성 | 대상 DB 행이 남으면 재시도 | 같은 리뷰·temp 키는 같은 목적지, 존재하면 덮어쓰지 않음 | 같은 tag 설정은 반복 가능하나 오래된 PUT 재사용 시 상태 덮어쓰기 고려 필요 |
| 운영 | 애플리케이션 스케줄러 운영 | prefix 규칙 + 복사 권한 + 롤아웃 점검 | tag IAM/서명/헤더/CORS + 상태 변경 순서 관리 |
| 스케줄러 제거 | 불가 | 기존 DB 행과 legacy S3 책임 때문에 이번에는 유지 | DB 행 정리는 역시 별도로 필요 |

**선택: 리뷰에 한정한 임시 prefix + 커밋 전 영구 복사.**
기존 API가 URL 문자열만 반환하므로 tag용 필수 헤더 계약을 늘리지 않는다. 별도 테이블, 큐,
Saga나 분산 트랜잭션 없이 DB 커밋 시점에 임시 키를 참조하지 않는다는 조건을 유지한다.
이는 모든 종류의 orphan을 완전히 없애는 설계가 아니라, 기존에 놓치던 **업로드 후 요청 미도달**을
S3 자체가 처리하도록 개선하는 설계다. 정상 이미지 보호를 위해 롤백 후 영구 사본 누수를 허용한다.

## 4. 구현된 업로드 → 확정 흐름

1. 로그인한 사용자가 기존 PUT API를 호출한다. `category=review`이면
   `temp/review/{memberId}/{uuid}.webp` 키를 발급한다. 사용자 ID는 인증 principal에서 가져온다.
2. 클라이언트가 기존과 같이 `Content-Type: image/webp`로 PUT한다. 발급 단계에는 DB 기록이 없다.
3. 리뷰 등록의 `imageUrls` 또는 수정의 `addImageUrls`에 URL이 아닌 해당 임시 키를 보낸다.
4. 등록은 DB 트랜잭션 내에서 Review ID를 먼저 얻는다. 이 INSERT는 아직 커밋이 아니다.
5. `S3Service.confirmReviewImage`가 정확한 prefix/숫자 사용자 ID/UUID/webp 형식과 소유자를 확인한다.
6. 목적지는 `review/{reviewId}/{memberId}/{uuid}.webp`이다. HEAD로 목적지 존재를 확인하고,
   없으면 S3 COPY가 성공할 때까지 진행한다. 404 외 HEAD 오류는 복사로 우회하지 않는다.
7. DB `ReviewImage.url`에는 영구 키만 저장하고 커밋한다. temp 원본은 즉시 지우지 않는다.
8. 설정된 Lifecycle이 temp 원본을 삭제한다. 영구 키는 이 규칙과 일치하지 않는다.

수정·삭제는 리뷰 행에 비관적 쓰기 잠금을 잡아 같은 리뷰의 동시 수정을 직렬화한다.
수정에서 동일 temp 키를 다시 전달하면 같은 영구 키로 해석하며 중복 DB 이미지를 만들지 않는다.
`remainImageUrls`와 이미 확정된 추가 키는 현재 리뷰에 실제로 연결된 키만 허용한다.
기존 리뷰의 legacy 키는 유지할 수 있지만 신규 등록에 임의 영구 키를 넣는 것은 거부한다.

매물·상담사는 기존 업로드 키 형식을 유지한다. PUT category는 `review`, `property`, `counselor`만 허용한다.
매물 등록·수정에는 리뷰 temp 키 저장을 금지하여 다른 정상 도메인이 Lifecycle 대상 키를 참조하지 않게 한다.
상담사 이미지 쓰기를 추후 다시 구현할 때도 이 제한 또는 별도 확정 단계를 반드시 적용해야 한다.

리뷰 수정으로 제거하는 객체는 DB 커밋 후 삭제한다. 롤백 시 기존 S3 이미지는 삭제하지 않는다.
멀티 삭제 응답의 개별 오류도 감지하며 커밋 후 삭제 실패는 키를 포함해 오류 로그로 남긴다.
이 로그는 자동 재시도 큐가 아니므로 운영 재처리가 필요하다.

## 5. 실패 시나리오

| 상황 | 결과 |
|---|---|
| A. 업로드 성공, DB 요청 없음 | DB 추적 여부와 무관하게 temp 원본을 Lifecycle이 정리 |
| B. 복사 전 DB 실패 | temp 원본만 남아 Lifecycle 정리 |
| B. 복사 성공 후 DB 롤백/커밋 실패 | temp는 Lifecycle 대상 유지. 영구 사본은 남을 수 있음. 커밋 여부가 불확실한 객체를 자동 보상 삭제하지 않음 |
| C. DB 성공 후 확정 실패 | 그런 순서를 쓰지 않음. COPY 실패를 전파해 DB 트랜잭션을 롤백. 커밋된 DB에는 영구 키만 존재 |
| D. 같은 확정 재시도 | 같은 리뷰 ID와 temp 키는 같은 목적지. 목적지가 있으면 복사 생략. 수정 요청의 중복 이미지도 제거 |
| E. 이미 확정된 키 전달 | 해당 리뷰의 현재 이미지이면 허용; 신규 리뷰/다른 리뷰의 영구 키는 거부 |
| F. 타 사용자 temp 키 | 키의 사용자 namespace가 인증된 작성자와 다르면 AWS 호출 전에 거부 |
| G. 임의 키/다른 prefix/URL/비정상 UUID | 신규 리뷰 이미지 확정에서 거부. 문법상 맞아도 원본이 없다면 COPY 실패로 DB 롤백 |
| 원본이 Lifecycle로 먼저 만료됨 | 목적지가 있으면 재시도 성공. 없고 COPY도 실패하면 DB 저장 실패; 새 업로드 필요 |
| PUT URL 재사용 | temp만 변경 가능. 이미 확정된 목적지는 HEAD 성공 시 덮어쓰지 않음 |
| 응답 손실 | 같은 리뷰 수정은 재시도 가능. 리뷰 생성 HTTP API 전체의 중복 방지/idempotency key는 이번에 추가하지 않음 |
| 리뷰 수정 DB 롤백 | 기존 객체 삭제는 afterCommit이라 실행되지 않음 |
| 커밋 후 삭제 오류/프로세스 종료 | 불필요한 영구 객체가 남을 수 있음. 정상 DB 이미지를 삭제하는 방향의 보상은 하지 않음 |

소유권 방어는 **이 애플리케이션이 서명한 경로로만 클라이언트가 쓰기 가능하다**는 전제다.
DB 업로드 발급 장부나 콘텐츠 검증은 없으며 AWS 쓰기 자격 증명을 가진 운영자를 방어하는 기능이 아니다.

## 6. 스케줄러의 유지 범위

신규 API는 임시 ReviewImage 행을 생성하지 않는다. 그래도 기존 미연결 DB 행은 존재할 수 있어
`ReviewImageCleanupScheduler`, `findUnusedImagesBefore`, `existsFile`, `deleteFile`을 유지한다.

- `temp/review/` 행: DB 행만 삭제. S3 HEAD/DELETE는 호출하지 않음.
- 그 밖의 legacy 미연결 행: 기존 HEAD/DELETE 후 DB 정리를 유지.
- 정상 연결 행: 기존 JPQL 조건에 의해 제외.

따라서 신규 temp 객체의 S3 삭제와 스케줄러가 중복되지 않는다.
legacy 데이터를 정리하고 생성 경로가 없음을 운영 DB에서도 확인한 후 별도로 스케줄러 제거를 판단할 수 있다.
이번에 제거한 Job/Step/Reader/Writer는 없다. 원래 리뷰 정리는 그 구조가 아니었다.

## 7. AWS 콘솔 설정과 배포 순서

저장소에는 Bucket Lifecycle을 관리하는 Terraform/CloudFormation/CDK 정의가 없다.
애플리케이션 시작 시 규칙을 생성하지 않으며, 이번 작업에서 AWS 설정을 실제 변경하지 않았다.

### 먼저 확인할 사항

1. 기존 Lifecycle 규칙을 확인한다. 버킷 전체나 `review/`를 만료시키는 규칙이 없어야 한다.
2. 현재 DB의 정상 이미지가 아래 예약 prefix를 쓰지 않는지 확인한다. 과거 API는 category 제한이 없었으므로
   이름만 보고 해당 prefix가 비어 있다고 가정하면 안 된다. 아래 쿼리 중 하나라도 결과가 있으면 규칙 활성화를 중단하고
   해당 정상 객체를 안전한 prefix로 복사·DB 참조 전환 후 다시 검사한다. 무작정 삭제하지 않는다.

```sql
SELECT id, url FROM review_image WHERE review_id IS NOT NULL AND url LIKE 'temp/review/%';
SELECT id, url FROM property_image WHERE url LIKE 'temp/review/%';
SELECT id, url FROM counselor_image WHERE url LIKE 'temp/review/%';
```

3. 모든 서버를 새 버전으로 전환한다. 기존 키의 신규 리뷰 등록은 이제 거부되므로 진행 중인 이전 업로드는
   새 PUT URL로 재업로드하도록 클라이언트를 맞춘다. 혼합 버전 상태에서 규칙을 활성화하지 않는다.
4. 클라이언트가 확정 이후에는 조회 결과의 **영구 키**를 유지 목록에 사용하는지 점검한다.
   API 경로/응답 타입/PUT 헤더는 같지만 서버가 최종 키를 바꾸므로 temp 키를 계속 보관하면 안 된다.
5. 스테이징에서 PUT → 리뷰 저장 → 영구 GET과 실패/재시도를 확인한다. 아래 규칙을 적용한다.

### 규칙

S3 콘솔 → 해당 Bucket → Management → Lifecycle rules → Create lifecycle rule:

| 항목 | 값 |
|---|---|
| Rule name / ID | `Delete temporary review images` |
| Filter | Prefix = `temp/review/` (끝 `/` 포함) |
| Action | Expire current versions of objects |
| Days | **1일 권장** |
| Scope | `review/`, `property/`, `counselor/`에는 만료 규칙을 추가하지 않음 |

추천 근거: 현재 PUT 유효기간은 3분이고 기존 미연결 DB 정리는 30분이다. 이 흐름은 영구 초안 보관이 아닌
대화형 리뷰 등록이며, 일 단위 Lifecycle의 1일은 기존 30분보다 넉넉한 재시도 여유를 주면서
업로드 취소 객체의 보관을 제한한다. 실제 작성 시간 통계나 SLA는 저장소에 없으므로 이는 측정값이 아니라
현재 정책 기반 추천이다. 하루 이상 작성 재개가 제품 요구라면 활성화 전에 보관 기간을 늘려야 한다.

Lifecycle은 정확히 업로드 후 24시간에 삭제되는 타이머가 아니다. 일수 기준은 UTC 일 경계로 계산되고
실제 처리는 비동기이므로 앱이 객체 삭제 시점을 전제로 동작하면 안 된다.
[AWS 만료 동작](https://docs.aws.amazon.com/AmazonS3/latest/userguide/lifecycle-expire-general-considerations.html),
[AWS Lifecycle 문제 해결](https://docs.aws.amazon.com/AmazonS3/latest/userguide/troubleshoot-lifecycle.html).

버전 관리가 꺼진 버킷의 규칙 예시(전체 Bucket 설정을 덮어쓰는 용도가 아님):

```json
{
  "ID": "Delete temporary review images",
  "Status": "Enabled",
  "Filter": { "Prefix": "temp/review/" },
  "Expiration": { "Days": 1 }
}
```

버전 관리가 켜져 있으면 current 만료만으로 저장된 모든 버전이 제거되지 않는다. 같은 prefix에
`NoncurrentVersionExpiration: { "NoncurrentDays": 1 }`도 설정하고, 필요하면 별도 규칙에서
expired delete marker 정리를 설정한다. 버전 복원/감사 요구와 충돌하지 않는지 먼저 확인한다.
PUT 재사용도 이전 temp 버전을 남길 수 있으므로 이 항목을 빠뜨리지 않는다.
[AWS Lifecycle 설정 예시](https://docs.aws.amazon.com/AmazonS3/latest/userguide/lifecycle-configuration-examples.html).

애플리케이션 역할에는 temp 원본 GetObject, review 목적지 PutObject/GetObject(HEAD), 기존 삭제에 필요한
DeleteObject 권한이 필요하다. CopyObject는 원본 읽기와 목적지 쓰기 권한을 사용한다.
SSE-KMS를 쓰는 환경은 원본 복호화/목적지 암호화에 필요한 KMS 권한도 확인한다.
HEAD의 없는 키를 404로 판별할 수 있도록 필요한 범위의 ListBucket 권한도 확인한다.
Object tag 권한이나 애플리케이션의 PutBucketLifecycleConfiguration 권한은 추가하지 않는다.
[AWS CopyObject 권한](https://docs.aws.amazon.com/AmazonS3/latest/API/API_CopyObject.html),
[AWS HEAD의 403/404 구분](https://docs.aws.amazon.com/AmazonS3/latest/API/API_HeadObject.html).

## 8. 테스트와 남는 한계

추가 테스트:

- `S3ServiceTest`: 실제 SDK presigner로 PUT/GET 서명, 키·사용자·prefix 검증, COPY/재시도/실패,
  매물의 temp 참조 방지, 커밋 후 삭제와 롤백, S3 멀티 삭제의 개별 오류.
- `S3ControllerTest`: 기존 HTTP 경로/파라미터/문자열 응답과 인증 principal 전달.
- `ReviewImageTransactionTest`: 실제 H2/JPA 트랜잭션의 등록·확정·롤백·중복 수정·이미지 제거 시점.
  복사 후 DB 롤백의 영구 사본 누수도 숨기지 않고 명시적으로 검증한다.
- `ReviewImageCleanupSchedulerTest`: temp는 DB만 삭제, legacy는 기존 순서 유지, S3 오류 시 DB 유지.

실행 명령: `.\gradlew.bat test --no-daemon`.
2026-09-11 최종 실행 결과: **106 tests, 0 failures, 0 errors, 0 skipped**, `BUILD SUCCESSFUL` (51초).
신규 테스트는 39개이며 기존 테스트 67개도 함께 통과했다.
실제 AWS Bucket에서 Lifecycle 경과를 기다리는 통합 테스트는 수행하지 않는다.

남는 범위: 복사 후 DB 롤백의 영구 orphan, 커밋 후 삭제 실패, 기존 리뷰 전체 삭제의 S3 누수,
매물·상담사 업로드 취소 객체, 과거 DB 없는 객체는 이번 temp 규칙으로 해결되지 않는다.
정상 데이터의 보관을 우선하여 영구 prefix에 포괄 만료를 추가하지 않는다.
향후 실제 누수량을 측정한 뒤 삭제 의도 기록 및 재시도, Inventory 기반 운영 점검 등을 별도 검토한다.
또한 S3 네트워크 호출 동안 DB 트랜잭션/리뷰 잠금을 유지하는 지연 비용이 있다.
파일 크기·내용 검증 및 공개된 GET 발급 API의 접근 정책은 이번 변경 범위가 아니다.

포트폴리오에는 “기존 Private + Presigned 구조를 유지하고, DB 행이 없는 미완료 리뷰 업로드까지
Lifecycle이 정리하도록 책임을 분리했다”라고 설명할 수 있다. 모든 orphan 정리나 모든 스케줄러 제거를
완료했다고 설명하면 실제 구현과 다르다.

## 9. 변경 파일

| 파일 (저장소 기준) | 변경 |
|---|---|
| `src/main/java/auctionTalk/auction/utils/s3/S3Service.java` | 임시 키·검증·복사 확정·커밋 후 삭제·멀티 삭제 오류 처리 |
| `src/main/java/auctionTalk/auction/utils/s3/S3Controller.java` | PUT 발급에 인증 사용자 ID 전달 |
| `src/main/java/auctionTalk/auction/domain/review/service/ReviewServiceImpl.java` | 등록/수정 확정 연결, 중복 방지, 삭제 시점 조정 |
| `src/main/java/auctionTalk/auction/domain/review/repository/ReviewRepository.java` | 수정·삭제의 비관적 잠금 조회 |
| `src/main/java/auctionTalk/auction/domain/review/dto/request/ReviewCreateRequest.java` | 임시 Object Key 계약 명시 |
| `src/main/java/auctionTalk/auction/domain/review/dto/request/ReviewUpdateRequest.java` | 유지/추가 Object Key 계약 명시 |
| `src/main/java/auctionTalk/auction/domain/property/service/AdminPropertyServiceImpl.java` | 리뷰 temp 키를 매물로 저장하는 경로 차단 |
| `src/main/java/auctionTalk/auction/batch/review/ReviewImageCleanupScheduler.java` | temp S3 삭제를 Lifecycle에 위임 |
| `src/test/java/auctionTalk/auction/utils/s3/S3ServiceTest.java` | 서명/키/확정/삭제 검증 |
| `src/test/java/auctionTalk/auction/utils/s3/S3ControllerTest.java` | HTTP 계약 검증 |
| `src/test/java/auctionTalk/auction/domain/review/service/ReviewImageTransactionTest.java` | H2 트랜잭션 검증 |
| `src/test/java/auctionTalk/auction/batch/review/ReviewImageCleanupSchedulerTest.java` | 정리 책임 분리 검증 |
| `docs/s3-image-lifecycle.md` | 설계 판단, 실패 조건, 배포/운영 문서 |

결제 리팩토링 코드와 의존성, Bucket 접근 방식, Presigned GET 발급 구현은 변경하지 않는다.
