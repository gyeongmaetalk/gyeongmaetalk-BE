package auctionTalk.auction.domain.member.client;

import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.domain.member.dto.client.KakaoMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoMemberClient {

    private final WebClient webClient;

    public String getKakaoUserId(String accessToken) {
        // 🔹 1. accessToken 검증 (유효하지 않으면 예외 발생)
        validateKakaoToken(accessToken);

        // 🔹 2. 사용자 정보 가져오기
        KakaoMemberResponse response = webClient
                .get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoMemberResponse.class)
                .block();

        if (response != null) {
            return response.getId();
        }
        throw new CustomApiException(ErrorCode.INVALID_KAKAO_TOKEN);
    }

    private void validateKakaoToken(String accessToken) {
        try {
            webClient.get()
                    .uri("https://kapi.kakao.com/v1/user/access_token_info")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity()
                    .block();  // 🔹 토큰이 유효하지 않으면 여기서 예외 발생
        } catch (Exception e) {
            throw new CustomApiException(ErrorCode.INVALID_KAKAO_TOKEN);
        }
    }
}
