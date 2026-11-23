package auctionTalk.auction.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCursorResponse<T> {

    private List<T> reviews;

    @Schema(description = "다음 페이지 요청에 넘길 커서. 마지막 페이지면 null", example = "MjAyNi0xMS0xNlQxMDowMHwxMjM")
    private String nextCursor;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;
}
