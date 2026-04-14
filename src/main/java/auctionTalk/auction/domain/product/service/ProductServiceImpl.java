package auctionTalk.auction.domain.product.service;

import auctionTalk.auction.domain.product.dto.response.ProductDetailResponse;
import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.domain.product.mapper.ProductMapper;
import auctionTalk.auction.domain.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDetailResponse getProduct(String code) {

        Product product = productRepository.getProductByCode(code);

        return productMapper.toProductDetailResponse(product);

    }

    @Override
    public List<ProductDetailResponse> getProducts() {
        return productRepository.findAllWithComponents().stream()
                .map(productMapper::toProductDetailResponse)
                .toList();
    }
}
