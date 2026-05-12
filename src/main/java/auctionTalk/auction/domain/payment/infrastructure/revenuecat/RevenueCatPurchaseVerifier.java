package auctionTalk.auction.domain.payment.infrastructure.revenuecat;

import auctionTalk.auction.domain.payment.dto.response.RevenueCatCustomerResponse;
import auctionTalk.auction.domain.payment.infrastructure.revenuecat.dto.RevenueCatNonSubscriptionPurchase;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RevenueCatPurchaseVerifier {

    public RevenueCatVerifiedPurchase verifyNonSubscriptionPurchase(
            RevenueCatCustomerResponse response,
            String productIdentifier,
            String transactionIdentifier
    ) {
        log.info("[REVENUECAT_VERIFY_REQUEST] productIdentifier={}, requestedTransactionIdentifier={}",
                productIdentifier,
                transactionIdentifier
        );

        if (response == null || response.getSubscriber() == null) {
            log.warn("[REVENUECAT_VERIFY_FAILED] response or subscriber is null. productIdentifier={}, requestedTransactionIdentifier={}",
                    productIdentifier,
                    transactionIdentifier
            );

            throw new CustomApiException(ErrorCode.REVENUECAT_VERIFICATION_FAILED);
        }

        Map<String, List<RevenueCatNonSubscriptionPurchase>> nonSubscriptions =
                response.getSubscriber().getNonSubscriptions();

        if (nonSubscriptions == null || nonSubscriptions.isEmpty()) {
            log.warn("[REVENUECAT_VERIFY_FAILED] nonSubscriptions is empty. productIdentifier={}, requestedTransactionIdentifier={}",
                    productIdentifier,
                    transactionIdentifier
            );

            throw new CustomApiException(ErrorCode.REVENUECAT_PURCHASE_NOT_FOUND);
        }

        log.info("[REVENUECAT_NON_SUBSCRIPTIONS_FOUND] availableProductIds={}",
                nonSubscriptions.keySet()
        );

        nonSubscriptions.forEach((productId, purchases) -> {
            log.info("[REVENUECAT_NON_SUBSCRIPTION_PRODUCT] productId={}, purchaseCount={}",
                    productId,
                    purchases == null ? 0 : purchases.size()
            );

            if (purchases != null) {
                purchases.forEach(purchase ->
                        log.info("[REVENUECAT_NON_SUBSCRIPTION_PURCHASE] productId={}, revenueCatPurchaseId={}, storeTransactionId={}, requestedTransactionIdentifier={}, store={}, sandbox={}, purchaseDate={}",
                                productId,
                                purchase.getId(),
                                purchase.getStoreTransactionId(),
                                transactionIdentifier,
                                purchase.getStore(),
                                purchase.getSandbox(),
                                purchase.getPurchaseDate()
                        )
                );
            }
        });

        List<RevenueCatNonSubscriptionPurchase> purchases =
                nonSubscriptions.getOrDefault(productIdentifier, List.of());

        if (purchases.isEmpty()) {
            log.warn("[REVENUECAT_PRODUCT_PURCHASE_EMPTY] requestedProductIdentifier={}, availableProductIds={}",
                    productIdentifier,
                    nonSubscriptions.keySet()
            );
        }

        RevenueCatNonSubscriptionPurchase matchedPurchase = purchases.stream()
                .filter(purchase -> matchesTransaction(purchase, transactionIdentifier))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("[REVENUECAT_PURCHASE_NOT_FOUND] requestedProductIdentifier={}, requestedTransactionIdentifier={}, availableProductIds={}",
                            productIdentifier,
                            transactionIdentifier,
                            nonSubscriptions.keySet()
                    );

                    return new CustomApiException(ErrorCode.REVENUECAT_PURCHASE_NOT_FOUND);
                });

        String verifiedTransactionIdentifier = resolveTransactionIdentifier(matchedPurchase);

        log.info("[REVENUECAT_PURCHASE_VERIFIED] productIdentifier={}, revenueCatPurchaseId={}, storeTransactionId={}, verifiedTransactionIdentifier={}, store={}, sandbox={}, purchaseDate={}",
                productIdentifier,
                matchedPurchase.getId(),
                matchedPurchase.getStoreTransactionId(),
                verifiedTransactionIdentifier,
                matchedPurchase.getStore(),
                matchedPurchase.getSandbox(),
                matchedPurchase.getPurchaseDate()
        );

        return RevenueCatVerifiedPurchase.builder()
                .productIdentifier(productIdentifier)
                .transactionIdentifier(verifiedTransactionIdentifier)
                .store(matchedPurchase.getStore())
                .sandbox(Boolean.TRUE.equals(matchedPurchase.getSandbox()))
                .purchasedAt(parsePurchaseDate(matchedPurchase.getPurchaseDate()))
                .build();
    }

    private boolean matchesTransaction(
            RevenueCatNonSubscriptionPurchase purchase,
            String transactionIdentifier
    ) {
        if (transactionIdentifier == null || transactionIdentifier.isBlank()) {
            return false;
        }

        return transactionIdentifier.equals(purchase.getStoreTransactionId())
                || transactionIdentifier.equals(purchase.getId());
    }

    private String resolveTransactionIdentifier(RevenueCatNonSubscriptionPurchase purchase) {
        if (purchase.getStoreTransactionId() != null && !purchase.getStoreTransactionId().isBlank()) {
            return purchase.getStoreTransactionId();
        }

        return purchase.getId();
    }

    private LocalDateTime parsePurchaseDate(String purchaseDate) {
        if (purchaseDate == null) {
            return LocalDateTime.now();
        }

        return OffsetDateTime.parse(purchaseDate).toLocalDateTime();
    }
}