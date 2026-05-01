package auctionTalk.auction.domain.product.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductType {
    SINGLE("단일 상품"),
    PACKAGE("패키지 상품");

    private final String description;
}