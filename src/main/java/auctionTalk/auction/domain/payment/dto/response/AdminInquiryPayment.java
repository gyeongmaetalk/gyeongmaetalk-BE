package auctionTalk.auction.domain.payment.dto.response;

import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminInquiryPayment {

    private LocalDateTime payDate;
    private Long amount;
    private String orderId;
    private String paymentKey;
    private String userName;
    private String cellPhone;
    private PaymentStatus paymentStatus;
}
