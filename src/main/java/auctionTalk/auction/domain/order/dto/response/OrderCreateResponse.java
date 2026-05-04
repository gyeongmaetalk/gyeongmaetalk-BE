package auctionTalk.auction.domain.order.dto.response;

import auctionTalk.auction.domain.order.entity.OrderStatus;
import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.domain.product.entity.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderCreateResponse {

    @Schema(description = "주문 ID", example = "1")
    private Long orderId;

    @Schema(description = "주문 번호", example = "ORD-20260415180000-AB12CD34")
    private String orderNumber;

    @Schema(description = "결제 금액", example = "300000")
    private Long amount;

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "상품 이름", example = "매물 열람권 10개")
    private String productName;

    @Schema(description = "스토어 상품 ID", example = "auction_forever_premium_ios")
    private String storeProductId;
}