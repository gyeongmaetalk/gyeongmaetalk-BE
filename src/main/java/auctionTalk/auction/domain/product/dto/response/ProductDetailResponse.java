package auctionTalk.auction.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ProductDetailResponse {

    @Schema(description = "상품 ID", example = "1")
    private Long id;

    @Schema(description = "상품명", example = "프리미엄 패키지")
    private String name;

    @Schema(description = "정가", example = "400000")
    private Long originalPrice;

    @Schema(description = "할인 가격", example = "387000")
    private Long price;

    @Schema(description = "추천 여부", example = "false")
    private boolean recommended;

    private List<ProductComponentResponse> components;
}
