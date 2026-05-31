package auctionTalk.auction.domain.product.service;

import auctionTalk.auction.domain.product.dto.response.ProductDetailResponse;
import auctionTalk.auction.domain.product.entity.ProductSearchCategory;

import java.util.List;

public interface ProductService {

    List<ProductDetailResponse> getProducts(ProductSearchCategory category);
    ProductDetailResponse getProduct(Long id);
}
