package auctionTalk.auction.domain.review.dto.request;

import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/**
 * 최신순 리뷰 목록의 커서. 마지막으로 받은 리뷰의 (작성 시각, ID)를 담는다.
 * 같은 시각에 작성된 리뷰가 있어도 ID로 순서가 정해지므로 누락·중복 없이 이어서 조회할 수 있다.
 * 클라이언트에는 내부 구조를 드러내지 않도록 Base64URL 문자열로 전달한다.
 */
public record ReviewCursor(LocalDateTime createdAt, Long id) {

    private static final String DELIMITER = "|";

    public String encode() {
        String raw = createdAt + DELIMITER + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static ReviewCursor decode(String encoded) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            int index = raw.lastIndexOf(DELIMITER);
            if (index <= 0) {
                throw new CustomApiException(ErrorCode.INVALID_REVIEW_CURSOR);
            }
            LocalDateTime createdAt = LocalDateTime.parse(raw.substring(0, index));
            long id = Long.parseLong(raw.substring(index + 1));
            return new ReviewCursor(createdAt, id);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new CustomApiException(ErrorCode.INVALID_REVIEW_CURSOR);
        }
    }
}
