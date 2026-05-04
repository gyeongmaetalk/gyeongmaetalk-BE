package auctionTalk.auction.domain.payment.infrastructure.revenuecat;

import auctionTalk.auction.domain.payment.dto.response.RevenueCatCustomerResponse;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class RevenueCatClient {

    private final WebClient revenueCatWebClient;

    public RevenueCatCustomerResponse getCustomer(String appUserId) {
        try {
            return revenueCatWebClient.get()
                    .uri("/subscribers/{appUserId}", appUserId)
                    .retrieve()
                    .bodyToMono(RevenueCatCustomerResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new CustomApiException(ErrorCode.REVENUECAT_API_FAILED);
        }
    }
}