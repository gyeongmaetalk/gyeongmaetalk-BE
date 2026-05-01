package auctionTalk.auction.domain.product.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductSearchCategory {
    ALL("전체 상품"),
    PACKAGE("패키지 상품"),
    VIEW_TICKET("열람권 상품");

    private final String description;
}