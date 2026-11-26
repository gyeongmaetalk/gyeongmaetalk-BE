package auctionTalk.auction.domain.review.dto;

import auctionTalk.auction.domain.review.dto.request.ReviewCursor;
import auctionTalk.auction.global.exception.CustomApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewCursorTest {

    @Test
    @DisplayName("마이크로초까지 포함한 작성 시각과 ID를 그대로 복원한다")
    void roundTrip() {
        ReviewCursor cursor = new ReviewCursor(LocalDateTime.of(2026, 11, 16, 10, 0, 0, 123_456_000), 42L);

        ReviewCursor decoded = ReviewCursor.decode(cursor.encode());

        assertThat(decoded).isEqualTo(cursor);
    }

    @Test
    @DisplayName("초 단위가 0이어도 복원된다")
    void roundTripWithoutSeconds() {
        ReviewCursor cursor = new ReviewCursor(LocalDateTime.of(2026, 1, 1, 0, 0), 1L);

        assertThat(ReviewCursor.decode(cursor.encode())).isEqualTo(cursor);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-base64!!", "", "MTIz"})
    @DisplayName("형식이 잘못된 커서는 400 예외로 처리한다")
    void invalidCursor(String encoded) {
        assertThatThrownBy(() -> ReviewCursor.decode(encoded))
                .isInstanceOf(CustomApiException.class);
    }

    @Test
    @DisplayName("ID가 숫자가 아니면 400 예외로 처리한다")
    void invalidId() {
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("2026-11-16T10:00|abc".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> ReviewCursor.decode(encoded))
                .isInstanceOf(CustomApiException.class);
    }
}
