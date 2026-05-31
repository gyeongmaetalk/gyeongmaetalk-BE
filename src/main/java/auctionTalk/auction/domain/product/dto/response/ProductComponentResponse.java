package auctionTalk.auction.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProductComponentResponse {

    @Schema(description = "상품 설명", example = "경매 대행 시작")
    private String description;

    @Schema(description = "갯수", example = "3")
    private Integer quantity;
}
