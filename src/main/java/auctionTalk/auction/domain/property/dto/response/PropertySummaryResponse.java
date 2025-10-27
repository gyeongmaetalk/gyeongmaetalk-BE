package auctionTalk.auction.domain.property.dto.response;

import auctionTalk.auction.domain.property.entity.PropertyImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PropertySummaryResponse {

    @Schema(description = "추천매물 ID", example = "1")
    private Long id;

    @Schema(description = "주소", example = "서울 도봉구")
    private String address;

    @Schema(description = "면적", example = "30")
    private Integer area;

    @Schema(description = "입찰일", example = "2025-09-18")
    private LocalDate biddingDate;

    @Schema(description = "감정가", example = "320000000")
    private Long appraisedPrice;

    @Schema(description = "최저가", example = "240000000")
    private Long minPrice;

    @Schema(description = "이미지 url 목록", example = "[\"url1\", \"url2\"]")
    private List<String> images;

    @Schema(description = "구매 완료 여부", example = "false")
    private boolean isPurchased;

    @Schema(description = "결제 완료 여부", example = "false")
    private boolean isPayment;

    @Schema(description = "건물 유형", example = "아파트")
    private String buildingType;

    @Schema(description = "업데이트 날짜", example = "2025-03-15")
    private LocalDateTime updateDate;
}
