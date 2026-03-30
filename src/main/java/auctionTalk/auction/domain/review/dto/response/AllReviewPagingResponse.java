package auctionTalk.auction.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllReviewPagingResponse <T> {

    private List<T> reviews;
    private int page;
    private int totalPages;
    private int totalElements;
    private Boolean isFirst;
    private Boolean isLast;
}
