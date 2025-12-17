package auctionTalk.auction.domain.subscription.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SubscriptionPagingResponse<T> {
    private List<T> subscriptions;
    private int page;
    private int totalPages;
    private int totalElements;
    private boolean isFirst;
    private boolean isLast;
}
