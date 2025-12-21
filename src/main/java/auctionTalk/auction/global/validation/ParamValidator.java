package auctionTalk.auction.global.validation;

import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;

public class ParamValidator {

    // 수정, 삭제 유효성 검사
    public static void validModify(Long memberId1, Long memberId2) {
        if (!memberId1.equals(memberId2))
            throw new CustomApiException(ErrorCode.UNAUTHORIZED_MODIFY);
    }

    // 이미 구독 중인 사람은 상담 신청 불가능
    public static void validMatchCounsel(boolean isSubscribed) {
        if (isSubscribed) {
            throw new CustomApiException(ErrorCode.MEMBER_IS_SUBSCRIBED);
        }
    }

    // FCM 토큰 유효성 검증
    public static void validateFcmToken(String token) {
        if (token == null || token.isBlank()) {
            throw new CustomApiException(ErrorCode.INVALID_FCM_TOKEN);
        }
    }
}
