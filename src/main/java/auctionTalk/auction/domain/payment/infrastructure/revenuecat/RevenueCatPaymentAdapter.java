package auctionTalk.auction.domain.payment.infrastructure.revenuecat;

import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.domain.payment.dto.response.PaymentVerificationResult;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.payment.service.verify.PaymentVerificationCommand;
import auctionTalk.auction.domain.payment.service.verify.PaymentVerificationService;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevenueCatPaymentAdapter implements PaymentVerificationService {

    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long RETRY_DELAY_MS = 1000L;

    private final RevenueCatClient revenueCatClient;
    private final RevenueCatPurchaseVerifier revenueCatPurchaseVerifier;
    private final MemberRepository memberRepository;

    @Override
    public PaymentProvider supportProvider() {
        return PaymentProvider.REVENUECAT;
    }

    @Override
    public PaymentVerificationResult verify(PaymentVerificationCommand command) {
        String appUserId = memberRepository
                .getMember(command.memberId())
                .getRevenueCatAppUserId();

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                RevenueCatVerifiedPurchase purchase =
                        revenueCatPurchaseVerifier.verifyNonSubscriptionPurchase(
                                revenueCatClient.getCustomer(appUserId),
                                command.productIdentifier(),
                                command.transactionIdentifier()
                        );

                return toVerificationResult(purchase);

            } catch (CustomApiException e) {
                if (!isRetryable(e) || attempt == MAX_RETRY_ATTEMPTS) {
                    throw e;
                }

                sleepBeforeRetry();
            }
        }

        throw new CustomApiException(ErrorCode.REVENUECAT_PURCHASE_NOT_FOUND);
    }

    private boolean isRetryable(CustomApiException e) {
        return e.getErrorCode() == ErrorCode.REVENUECAT_PURCHASE_NOT_FOUND
                || e.getErrorCode() == ErrorCode.REVENUECAT_VERIFICATION_FAILED;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomApiException(ErrorCode.REVENUECAT_VERIFICATION_FAILED);
        }
    }

    private PaymentVerificationResult toVerificationResult(
            RevenueCatVerifiedPurchase purchase
    ) {
        return PaymentVerificationResult.builder()
                .provider(supportProvider())
                .providerTransactionId(purchase.getTransactionIdentifier())
                .storeProductId(purchase.getProductIdentifier())
                .approvedAt(purchase.getPurchasedAt())
                .store(purchase.getStore())
                .sandbox(purchase.getSandbox())
                .build();
    }
}