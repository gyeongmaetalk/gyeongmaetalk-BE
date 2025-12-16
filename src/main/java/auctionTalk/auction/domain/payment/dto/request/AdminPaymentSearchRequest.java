package auctionTalk.auction.domain.payment.dto.request;

import auctionTalk.auction.domain.payment.entity.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminPaymentSearchRequest {

    private PaymentType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private int page;
    private int size;
}
