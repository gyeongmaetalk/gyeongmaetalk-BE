package auctionTalk.auction.domain.payment.service.verify;

import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentVerificationServiceResolver {

    private final Map<PaymentProvider, PaymentVerificationService> services = new EnumMap<>(PaymentProvider.class);

    public PaymentVerificationServiceResolver(List<PaymentVerificationService> verificationServices) {
        for (PaymentVerificationService service : verificationServices) {
            if (services.putIfAbsent(service.supportProvider(), service) != null) {
                throw new IllegalStateException("중복 결제 검증기: " + service.supportProvider());
            }
        }
    }

    public PaymentVerificationService resolve(PaymentProvider provider) {
        PaymentVerificationService service = services.get(provider);
        if (service == null) {
            throw new CustomApiException(ErrorCode.INVALID_PAYMENT_PROVIDER);
        }
        return service;
    }
}
