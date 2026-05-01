package auctionTalk.auction.domain.product.service;

import auctionTalk.auction.domain.product.dto.response.ProductDetailResponse;
import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.domain.product.entity.ProductSearchCategory;
import auctionTalk.auction.domain.product.entity.ProductType;
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
    public ProductDetailResponse getProduct(Long id) {

        Product product = productRepository.getProduct(id);

        return productMapper.toProductDetailResponse(product);
    }

    @Override
    @Transactional
    public List<ProductDetailResponse> getProducts(ProductSearchCategory category) {
        return productRepository.findAllWithComponents().stream()
                .filter(Product::isActive)
                .filter(product -> matchesCategory(product, category))
                .map(productMapper::toProductDetailResponse)
                .toList();
    }

    private boolean matchesCategory(Product product, ProductSearchCategory category) {
        return switch (category) {
            case ALL -> true;
            case PACKAGE -> product.getProductType() == ProductType.PACKAGE;
            case VIEW_TICKET -> product.getProductType() == ProductType.SINGLE;
        };
    }
}
