package auctionTalk.auction.domain.payment.infrastructure.revenuecat;

import auctionTalk.auction.domain.payment.dto.response.RevenueCatCustomerResponse;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevenueCatClient {

    private final WebClient revenueCatWebClient;
    private final ObjectMapper objectMapper;

    public RevenueCatCustomerResponse getCustomer(String appUserId) {
        String raw = revenueCatWebClient.get()
                .uri("/subscribers/{appUserId}", appUserId)
                .header("X-Is-Sandbox", "true")
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> {
                                    log.error("[REVENUECAT_ERROR_RESPONSE] status={}, appUserId={}, body={}",
                                            response.statusCode(), appUserId, body);
                                    return new CustomApiException(ErrorCode.REVENUECAT_VERIFICATION_FAILED);
                                })
                )
                .bodyToMono(String.class)
                .block();

        log.info("[REVENUECAT_RAW_RESPONSE] appUserId={}, raw={}", appUserId, raw);

        try {
            return objectMapper.readValue(raw, RevenueCatCustomerResponse.class);
        } catch (Exception e) {
            log.error("[REVENUECAT_PARSE_FAILED] appUserId={}, raw={}", appUserId, raw, e);
            throw new CustomApiException(ErrorCode.REVENUECAT_VERIFICATION_FAILED);
        }
    }
}