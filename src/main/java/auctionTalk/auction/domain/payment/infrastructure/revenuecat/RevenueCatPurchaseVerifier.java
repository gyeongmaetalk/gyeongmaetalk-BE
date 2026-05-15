package auctionTalk.auction.domain.payment.infrastructure.revenuecat;

import auctionTalk.auction.domain.payment.dto.response.RevenueCatCustomerResponse;
import auctionTalk.auction.domain.payment.dto.response.RevenueCatEntitlement;
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
            throw new CustomApiException(ErrorCode.REVENUECAT_VERIFICATION_FAILED);
        }

        RevenueCatCustomerResponse.RevenueCatSubscriber subscriber = response.getSubscriber();

        RevenueCatVerifiedPurchase nonSubscriptionResult =
                verifyByNonSubscriptions(subscriber, productIdentifier, transactionIdentifier);

        if (nonSubscriptionResult != null) {
            return nonSubscriptionResult;
        }

        RevenueCatVerifiedPurchase entitlementResult =
                verifyByEntitlements(subscriber, productIdentifier);

        if (entitlementResult != null) {
            return entitlementResult;
        }

        log.warn("[REVENUECAT_PURCHASE_NOT_FOUND] productIdentifier={}, transactionIdentifier={}",
                productIdentifier,
                transactionIdentifier
        );

        throw new CustomApiException(ErrorCode.REVENUECAT_PURCHASE_NOT_FOUND);
    }

    private RevenueCatVerifiedPurchase verifyByNonSubscriptions(
            RevenueCatCustomerResponse.RevenueCatSubscriber subscriber,
            String productIdentifier,
            String transactionIdentifier
    ) {
        Map<String, List<RevenueCatNonSubscriptionPurchase>> nonSubscriptions =
                subscriber.getNonSubscriptions();

        if (nonSubscriptions == null || nonSubscriptions.isEmpty()) {
            log.warn("[REVENUECAT_NON_SUBSCRIPTIONS_EMPTY] productIdentifier={}", productIdentifier);
            return null;
        }

        log.info("[REVENUECAT_NON_SUBSCRIPTIONS_FOUND] availableProductIds={}",
                nonSubscriptions.keySet()
        );

        List<RevenueCatNonSubscriptionPurchase> purchases =
                nonSubscriptions.getOrDefault(productIdentifier, List.of());

        if (purchases.isEmpty()) {
            log.warn("[REVENUECAT_PRODUCT_PURCHASE_EMPTY] requestedProductIdentifier={}, availableProductIds={}",
                    productIdentifier,
                    nonSubscriptions.keySet()
            );
            return null;
        }

        return purchases.stream()
                .filter(purchase -> matchesTransaction(purchase, transactionIdentifier))
                .findFirst()
                .map(purchase -> RevenueCatVerifiedPurchase.builder()
                        .productIdentifier(productIdentifier)
                        .transactionIdentifier(resolveTransactionIdentifier(purchase))
                        .store(purchase.getStore())
                        .sandbox(Boolean.TRUE.equals(purchase.getSandbox()))
                        .purchasedAt(parsePurchaseDate(purchase.getPurchaseDate()))
                        .build())
                .orElse(null);
    }

    private RevenueCatVerifiedPurchase verifyByEntitlements(
            RevenueCatCustomerResponse.RevenueCatSubscriber subscriber,
            String productIdentifier
    ) {
        Map<String, RevenueCatEntitlement> entitlements = subscriber.getEntitlements();

        if (entitlements == null || entitlements.isEmpty()) {
            log.warn("[REVENUECAT_ENTITLEMENTS_EMPTY] productIdentifier={}", productIdentifier);
            return null;
        }

        log.info("[REVENUECAT_ENTITLEMENTS_FOUND] entitlementIds={}", entitlements.keySet());

        RevenueCatEntitlement matchedEntitlement = entitlements.values().stream()
                .filter(entitlement -> productIdentifier.equals(entitlement.getProductIdentifier()))
                .filter(this::isActiveEntitlement)
                .findFirst()
                .orElse(null);

        if (matchedEntitlement == null) {
            log.warn("[REVENUECAT_ENTITLEMENT_NOT_FOUND] productIdentifier={}, entitlementIds={}",
                    productIdentifier,
                    entitlements.keySet()
            );
            return null;
        }

        log.info("[REVENUECAT_ENTITLEMENT_VERIFIED] productIdentifier={}, store={}, purchaseDate={}, expiresDate={}",
                matchedEntitlement.getProductIdentifier(),
                matchedEntitlement.getStore(),
                matchedEntitlement.getPurchaseDate(),
                matchedEntitlement.getExpiresDate()
        );

        return RevenueCatVerifiedPurchase.builder()
                .productIdentifier(productIdentifier)
                .transactionIdentifier(null)
                .store(matchedEntitlement.getStore())
                .sandbox(true)
                .purchasedAt(parsePurchaseDate(matchedEntitlement.getPurchaseDate()))
                .build();
    }

    private boolean isActiveEntitlement(RevenueCatEntitlement entitlement) {
        String expiresDate = entitlement.getExpiresDate();

        if (expiresDate == null || expiresDate.isBlank()) {
            return true;
        }

        return OffsetDateTime.parse(expiresDate).isAfter(OffsetDateTime.now());
    }

    private boolean matchesTransaction(
            RevenueCatNonSubscriptionPurchase purchase,
            String transactionIdentifier
    ) {
        if (transactionIdentifier == null || transactionIdentifier.isBlank()) {
            return false;
        }

        String storeTransactionId = purchase.getStoreTransactionId();
        String revenueCatPurchaseId = purchase.getId();

        return transactionIdentifier.equals(storeTransactionId)
                || transactionIdentifier.equals(revenueCatPurchaseId)
                || endsWithTransactionNumber(storeTransactionId, transactionIdentifier)
                || endsWithTransactionNumber(revenueCatPurchaseId, transactionIdentifier);
    }

    private boolean endsWithTransactionNumber(String revenueCatTransactionId, String transactionIdentifier) {
        if (revenueCatTransactionId == null || revenueCatTransactionId.isBlank()) {
            return false;
        }

        return revenueCatTransactionId.endsWith("_" + transactionIdentifier);
    }

    private String resolveTransactionIdentifier(RevenueCatNonSubscriptionPurchase purchase) {
        if (purchase.getStoreTransactionId() != null && !purchase.getStoreTransactionId().isBlank()) {
            return purchase.getStoreTransactionId();
        }

        return purchase.getId();
    }

    private LocalDateTime parsePurchaseDate(String purchaseDate) {
        if (purchaseDate == null || purchaseDate.isBlank()) {
            return LocalDateTime.now();
        }

        return OffsetDateTime.parse(purchaseDate).toLocalDateTime();
    }
}