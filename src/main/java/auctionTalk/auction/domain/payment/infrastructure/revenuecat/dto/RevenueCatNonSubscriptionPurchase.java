package auctionTalk.auction.domain.payment.infrastructure.revenuecat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevenueCatNonSubscriptionPurchase {

    /**
     * RevenueCat 구매 ID
     * non_subscriptions 구매 내역의 id 값
     */
    private String id;

    /**
     * 구매 스토어
     * 예: app_store, play_store
     */
    private String store;

    /**
     * 구매 일시
     * 예: 2019-04-05T21:52:45Z
     */
    @JsonProperty("purchase_date")
    private String purchaseDate;

    /**
     * 샌드박스 결제 여부
     */
    @JsonProperty("is_sandbox")
    private Boolean sandbox;

    @JsonProperty("store_transaction_id")
    private String storeTransactionId;
}