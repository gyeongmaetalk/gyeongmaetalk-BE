package auctionTalk.auction.domain.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PaymentVerificationResult {

    private final String providerTransactionId;
    private final String storeProductId;
    private final LocalDateTime approvedAt;
    private final String rawVerificationData;
}