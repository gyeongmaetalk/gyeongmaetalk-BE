package auctionTalk.auction.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentFulfillmentStatus {
    PENDING("대기중"),
    SUCCESS("지급 완료");

    private final String description;

    }