package auctionTalk.auction.domain.payment.service.verify;

import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentConfirmResponse;

public interface PaymentConfirmService {

    PaymentConfirmResponse confirm(Long memberId, PaymentConfirmRequest request);
    String generate();
}
