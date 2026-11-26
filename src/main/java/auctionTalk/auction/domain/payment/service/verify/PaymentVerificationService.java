package auctionTalk.auction.domain.payment.service.verify;

import auctionTalk.auction.domain.payment.dto.response.PaymentVerificationResult;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;

public interface PaymentVerificationService {

    PaymentProvider supportProvider();
    PaymentVerificationResult verify(PaymentVerificationCommand command);
}
