package auctionTalk.auction.domain.payment.service.verify.apple;

import auctionTalk.auction.domain.payment.dto.response.PaymentVerificationResult;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.payment.service.verify.PaymentVerificationService;
import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.apple.itunes.storekit.verification.VerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApplePaymentVerificationService implements PaymentVerificationService {

    private final SignedDataVerifier signedDataVerifier;

    @Override
    public PaymentProvider supportProvider() {
        return PaymentProvider.APPLE;
    }

    @Override
    public PaymentVerificationResult verify(PaymentConfirmRequest request) {
        try {
            JWSTransactionDecodedPayload transaction =
                    signedDataVerifier.verifyAndDecodeTransaction(request.getProviderVerificationData());

            if (!Objects.equals(transaction.getProductId(), request.getStoreProductId())) {
                throw new IllegalStateException("애플 상품 ID가 주문 상품과 다릅니다.");
            }

            return PaymentVerificationResult.builder()
                    .providerTransactionId(transaction.getTransactionId())
                    .storeProductId(transaction.getProductId())
                    .approvedAt(toLocalDateTime(transaction.getPurchaseDate()))
                    .rawVerificationData(request.getProviderVerificationData())
                    .build();

        } catch (VerificationException e) {
            throw new IllegalStateException("애플 결제 검증 실패", e);
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
}
