package auctionTalk.auction.domain.payment.service.verify;

import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentVerificationServiceResolver {

    private final Map<PaymentProvider, PaymentVerificationService> services = new EnumMap<>(PaymentProvider.class);

    public PaymentVerificationServiceResolver(List<PaymentVerificationService> verificationServices) {
        for (PaymentVerificationService service : verificationServices) {
            services.put(service.supportProvider(), service);
        }
    }

    public PaymentVerificationService resolve(PaymentProvider provider) {
        PaymentVerificationService service = services.get(provider);
        if (service == null) {
            throw new IllegalArgumentException("지원하지 않는 결제 제공자입니다: " + provider);
        }
        return service;
    }
}