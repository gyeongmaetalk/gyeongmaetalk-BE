package auctionTalk.auction.domain.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevenueCatEntitlement {

    @JsonProperty("product_identifier")
    private String productIdentifier;

    @JsonProperty("purchase_date")
    private String purchaseDate;

    @JsonProperty("expires_date")
    private String expiresDate;

    @JsonProperty("store")
    private String store;
}