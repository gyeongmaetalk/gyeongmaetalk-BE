package auctionTalk.auction.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON402", "금지된 요청입니다."),
    UNAUTHORIZED_MODIFY(HttpStatus.BAD_REQUEST, "COMMON403", "수정, 삭제 권한이 없습니다."),

    // JWT Token
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT401", "잘못된 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT402", "만료된 JWT 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT403", "지원하지 않는 JWT 토큰입니다."),
    EMPTY_JWT_CLAIMS(HttpStatus.UNAUTHORIZED, "JWT404", "JWT claims string is empty입니다."),
    UNAUTHORIZED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "JWT405", "권한 정보가 없는 토큰입니다."),

    //LOGIN
    INVALID_LOGIN_TYPE(HttpStatus.BAD_REQUEST, "AUTH401", "잘못된 로그인 타입입니다."),
    INVALID_KAKAO_TOKEN(HttpStatus.BAD_REQUEST, "AUTH402", "잘못된 카카오 토큰입니다."),
    INVALID_APPLE_TOKEN(HttpStatus.BAD_REQUEST, "AUTH403", "잘못된 애플 토큰입니다."),
    EXPIRED_APPLE_TOKEN(HttpStatus.BAD_REQUEST, "AUTH404", "만료된 애플 토큰입니다."),
    INVALID_APPLE_TOKEN_AUDIENCE(HttpStatus.BAD_REQUEST, "AUTH405", "잘못된 애플 토큰 audience입니다."),
    INVALID_AUTH_CODE(HttpStatus.BAD_REQUEST, "AUTH405", "유효하지 않거나 만료된 인증 코드입니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER401", "사용자를 찾을 수 없습니다."),
    MEMBER_SOFT_DELETE(HttpStatus.BAD_REQUEST, "MEMBER402", "회원 탈퇴된 사용자 입니다."),
    MEMBER_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER403", "패스워드가 일치하지 않습니다."),
    MEMBER_IS_PRESENT(HttpStatus.BAD_REQUEST, "MEMBER404", "이미 존재하는 계정입니다."),

    // Counselor
    COUNSELOR_NOT_FOUND(HttpStatus.NOT_FOUND, "COUNSELOR401", "상담사를 찾을 수 없습니다."),

    // Counsel
    COUNSEL_NOT_FOUND(HttpStatus.NOT_FOUND, "COUNSEL401", "상담을 찾을 수 없습니다."),

    // Counsel_Form
    COUNSEL_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "COUNSEL_FORM401", "상담 신청 폼을 찾을 수 없습니다."),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW401", "리뷰를 찾을 수 없습니다."),
    INVALID_REVIEW_SORT_TYPE(HttpStatus.BAD_REQUEST, "REVIEW402", "유효하지 않은 리뷰 정렬 타입 입니다."),

    // Property
    PROPERTY_NOT_FOUND(HttpStatus.NOT_FOUND, "PROPERTY401", "추천 매물을 찾을 수 없습니다."),

    // Subscription
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPTION401", "추천 매물 구독 정보를 찾을 수 없습니다."),

    // Qna
    QNA_NOT_FOUND(HttpStatus.NOT_FOUND, "QNA401", "질문을 찾을 수 없습니다."),

    // Firebase
    FIREBASE_INIT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FIREBASE500", "Firebase 초기화에 실패했습니다."),
    FIREBASE_PUSH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FIREBASE501", "Firebase 알림 푸쉬에 실패했습니다."),
    INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "FIREBASE401", "FCM 토큰이 비어 있습니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION401", "알림을 찾을 수 없습니다."),

    // Payment
    FAIL_CONFIRM_PAYMENT(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT401", "결제 승인에 실패했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT402", "결제 정보를 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
