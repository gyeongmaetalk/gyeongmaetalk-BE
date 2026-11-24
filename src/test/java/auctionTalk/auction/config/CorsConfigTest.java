package auctionTalk.auction.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsConfigTest {

    private final CorsConfig corsConfig = new CorsConfig();

    @Test
    @DisplayName("허용 출처를 설정하지 않으면 앱이 뜨지 않는다")
    void missingOriginsFailFast() {
        assertThatThrownBy(() -> corsConfig.corsConfigurationSource(new CorsProperties(null)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> corsConfig.corsConfigurationSource(new CorsProperties(List.of())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("모든 출처 허용(*)은 설정할 수 없다")
    void wildcardIsRejected() {
        assertThatThrownBy(() -> corsConfig.corsConfigurationSource(new CorsProperties(List.of("https://app.example.com", "*"))))
                .isInstanceOf(IllegalStateException.class);
    }
}
