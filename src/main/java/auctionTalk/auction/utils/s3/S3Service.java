package auctionTalk.auction.utils.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class S3Service {

    // 조회용 presigned URL 유효 시간. 응답을 캐시하는 쪽(RedisConfig)은 이보다 짧은 TTL을 써야 만료된 URL을 내보내지 않는다.
    public static final Duration PRESIGNED_GET_URL_TTL = Duration.ofMinutes(10);


    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String generatePresignedPutUrl(String category, String originalFileName) {
        String fileName = createFileName(category, Objects.requireNonNull(originalFileName));

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType("image/webp")
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                builder -> builder
                        .signatureDuration(Duration.ofMinutes(3))
                        .putObjectRequest(putObjectRequest)
        );

        return presignedRequest.url().toString();
    }

    public String generatePresignedGetUrl(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            return null;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                builder -> builder
                        .signatureDuration(PRESIGNED_GET_URL_TTL)
                        .getObjectRequest(getObjectRequest)
        );

        return presignedRequest.url().toString();
    }

    public void deleteFile(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    public void deleteFiles(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> objects = keys.stream()
                .filter(StringUtils::hasText)
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        if (objects.isEmpty()) {
            return;
        }

        Delete delete = Delete.builder()
                .objects(objects)
                .build();

        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(delete)
                .build();

        s3Client.deleteObjects(request);
    }

    public boolean existsFile(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }


    /**
     * 파일명 생성
     * @param category
     * @param originalFileName
     * @return 작명된 파일 이름
     */
    public String createFileName(String category, String originalFileName) {
        int fileExtensionIndex = originalFileName.lastIndexOf(".");
        String fileExtension = originalFileName.substring(fileExtensionIndex);
        String fileName = originalFileName.substring(0, fileExtensionIndex);
        String random = String.valueOf(UUID.randomUUID());

        return category + "/" + fileName + "_" + random + fileExtension;
    }


}