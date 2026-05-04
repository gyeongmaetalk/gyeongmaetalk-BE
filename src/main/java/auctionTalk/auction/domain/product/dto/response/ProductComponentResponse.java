package auctionTalk.auction.domain.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProductComponentResponse {

    @Schema(description = "구성요소 타입", example = "VIEW_TICKET")
    private String componentType;

    @Schema(description = "구성요소 이름", example = "매물 열람권 5장")
    private String name;

    @Schema(description = "구성요소 설명", example = "프리미엄 패키지의 열람권 구성요소")
    private String description;

    @Schema(description = "열람권 개수", example = "5", nullable = true)
    private Integer ticketCount;
}
