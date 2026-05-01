package auctionTalk.auction.domain.product.controller;

import auctionTalk.auction.domain.product.dto.response.ProductDetailResponse;
import auctionTalk.auction.domain.product.service.ProductService;
import auctionTalk.auction.domain.property.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "상품 API", description = "상품 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 목록 조회", description = "판매 중인 상품 목록을 조회합니다.")
    @GetMapping
    public List<ProductDetailResponse> getProducts() {
        return productService.getProducts();
    }

    @Operation(summary = "상품 단건 조회", description = "상품 코드를 통해 단건 상품 정보를 조회합니다.")
    @GetMapping("/{productId}")
    public ProductDetailResponse getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }
}
