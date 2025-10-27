package auctionTalk.auction.global.validation;

import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;

public class ParamValidator {

    // 수정, 삭제 유효성 검사
    public static void validModify(Long memberId1, Long memberId2) {
        if (!memberId1.equals(memberId2))
            throw new CustomApiException(ErrorCode.UNAUTHORIZED_MODIFY);
    }

    // FCM 토큰 유효성 검증
    public static void validateFcmToken(String token) {
        if (token == null || token.isBlank()) {
            throw new CustomApiException(ErrorCode.INVALID_FCM_TOKEN);
        }
    }
}
