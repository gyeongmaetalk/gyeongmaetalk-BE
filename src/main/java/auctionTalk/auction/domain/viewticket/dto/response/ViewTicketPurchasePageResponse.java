package auctionTalk.auction.domain.viewticket.dto.response;

import auctionTalk.auction.domain.product.dto.response.ProductDetailResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "열람권 구매 화면 조회 응답")
public class ViewTicketPurchasePageResponse {

    @Schema(description = "회원의 잔여 열람권 개수", example = "3")
    private Integer balance;

    @Schema(description = "판매 중인 열람권 상품 목록")
    private List<ProductDetailResponse> products;
}