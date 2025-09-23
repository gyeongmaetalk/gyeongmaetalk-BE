package auctionTalk.auction.domain.review.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewSortType {
    LATEST("최신순"),
    OLDEST("오래된 순"),
    HIGHEST_SCORE("별점 높은 순"),
    LOWEST_SCORE("별점 낮은 순");

    private final String description;
}