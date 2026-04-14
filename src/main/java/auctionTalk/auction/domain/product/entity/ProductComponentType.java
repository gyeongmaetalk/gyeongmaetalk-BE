package auctionTalk.auction.domain.product.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductComponentType {
    SUBSCRIPTION("경매 대행"),
    VIEW_TICKET("매물 열람권");

    private final String description;

    }