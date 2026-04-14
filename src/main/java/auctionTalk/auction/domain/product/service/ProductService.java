package auctionTalk.auction.domain.product.service;

import auctionTalk.auction.domain.product.dto.response.ProductDetailResponse;

import java.util.List;

public interface ProductService {

    List<ProductDetailResponse> getProducts();
    ProductDetailResponse getProduct(String code);
}
