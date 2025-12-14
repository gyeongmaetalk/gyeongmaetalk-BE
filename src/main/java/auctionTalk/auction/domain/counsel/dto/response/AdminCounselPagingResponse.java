package auctionTalk.auction.domain.counsel.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminCounselPagingResponse<T> {
    private List<T> counsels;

    @Schema(description = "현재 페이지 번호", example = "1")
    private int page;

    @Schema(description = "총 페이지 수", example = "10")
    private int totalPages;

    @Schema(description = "총 요소 수", example = "100")
    private int totalElements;

    @Schema(description = "첫 번째 페이지 여부", example = "true")
    private Boolean isFirst;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private Boolean isLast;
}
