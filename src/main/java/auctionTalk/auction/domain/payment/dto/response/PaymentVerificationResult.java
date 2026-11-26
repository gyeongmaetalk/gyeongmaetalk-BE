package auctionTalk.auction.domain.payment.dto.response;

import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** 검증 성공 결과만 반환한다. 검증 실패는 예외로 전달하고 주문 상태는 변경하지 않는다. */
@Getter
@Builder
public class PaymentVerificationResult {
    private final PaymentProvider provider;
    private final String providerTransactionId;
    private final String storeProductId;
    private final LocalDateTime approvedAt;
    private final Long approvedAmount;
    private final String store;
    private final Boolean sandbox;
}
