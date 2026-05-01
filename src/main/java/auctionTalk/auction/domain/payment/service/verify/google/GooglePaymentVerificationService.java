package auctionTalk.auction.domain.payment.service.verify.google;

import auctionTalk.auction.domain.payment.dto.response.PaymentVerificationResult;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.payment.service.verify.PaymentVerificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GooglePaymentVerificationService implements PaymentVerificationService {

    private final AndroidPublisher androidPublisher;
    private final ObjectMapper objectMapper;

    @Value("${payment.google.package-name}")
    private String packageName;

    @Override
    public PaymentProvider supportProvider() {
        return PaymentProvider.GOOGLE;
    }

    @Override
    public PaymentVerificationResult verify(PaymentConfirmRequest request) {
        try {
            ProductPurchase purchase = androidPublisher.purchases().products()
                    .get(
                            packageName,
                            request.getStoreProductId(),
                            request.getProviderVerificationData()
                    )
                    .execute();

            if (!Objects.equals(purchase.getPurchaseState(), 0)) {
                throw new IllegalStateException("구글 결제가 완료 상태가 아닙니다.");
            }

            return PaymentVerificationResult.builder()
                    .providerTransactionId(purchase.getOrderId())
                    .storeProductId(request.getStoreProductId())
                    .approvedAt(toLocalDateTime(purchase.getPurchaseTimeMillis()))
                    .rawVerificationData(writeJson(purchase))
                    .build();

        } catch (IOException e) {
            throw new IllegalStateException("구글 결제 검증 실패", e);
        }
    }

    private LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMillis),
                ZoneId.systemDefault()
        );
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}