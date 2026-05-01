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

    @Schema(description = "상품 설명", example = "월 구독 + 매물 열람권 5장")
    private String description;

    @Schema(description = "상품 타입", example = "PACKAGE")
    private String productType;

    @Schema(description = "상품 가격", example = "387000")
    private Long price;

    private List<ProductComponentResponse> components;
}
