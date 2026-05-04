package auctionTalk.auction.domain.payment.infrastructure.revenuecat;

import auctionTalk.auction.domain.payment.dto.response.RevenueCatCustomerResponse;
import auctionTalk.auction.domain.payment.infrastructure.revenuecat.dto.RevenueCatNonSubscriptionPurchase;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
public class RevenueCatPurchaseVerifier {

    public RevenueCatVerifiedPurchase verifyNonSubscriptionPurchase(
            RevenueCatCustomerResponse response,
            String productIdentifier,
            String transactionIdentifier
    ) {
        if (response == null || response.getSubscriber() == null) {
            throw new CustomApiException(ErrorCode.REVENUECAT_VERIFICATION_FAILED);
        }

        Map<String, List<RevenueCatNonSubscriptionPurchase>> nonSubscriptions =
                response.getSubscriber().getNonSubscriptions();

        List<RevenueCatNonSubscriptionPurchase> purchases =
                nonSubscriptions.getOrDefault(productIdentifier, List.of());

        RevenueCatNonSubscriptionPurchase matchedPurchase = purchases.stream()
                .filter(purchase -> matchesTransaction(purchase, transactionIdentifier))
                .findFirst()
                .orElseThrow(() -> new CustomApiException(ErrorCode.REVENUECAT_PURCHASE_NOT_FOUND));

        return RevenueCatVerifiedPurchase.builder()
                .productIdentifier(productIdentifier)
                .transactionIdentifier(transactionIdentifier)
                .store(matchedPurchase.getStore())
                .sandbox(Boolean.TRUE.equals(matchedPurchase.getSandbox()))
                .purchasedAt(parsePurchaseDate(matchedPurchase.getPurchaseDate()))
                .build();
    }

    private boolean matchesTransaction(
            RevenueCatNonSubscriptionPurchase purchase,
            String transactionIdentifier
    ) {
        return transactionIdentifier.equals(purchase.getId())
                || transactionIdentifier.equals(purchase.getStoreTransactionId());
    }

    private LocalDateTime parsePurchaseDate(String purchaseDate) {
        if (purchaseDate == null) {
            return LocalDateTime.now();
        }

        return OffsetDateTime.parse(purchaseDate).toLocalDateTime();
    }
}