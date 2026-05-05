package auctionTalk.auction.domain.product.mapper;

import auctionTalk.auction.domain.product.dto.response.ProductComponentResponse;
import auctionTalk.auction.domain.product.dto.response.ProductDetailResponse;
import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.domain.product.entity.ProductComponent;
import auctionTalk.auction.domain.product.entity.ProductComponentMapping;
import auctionTalk.auction.domain.product.entity.ProductComponentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    public ProductDetailResponse toProductDetailResponse(Product product) {
        List<ProductComponentResponse> components = product.getComponentMappings().stream()
                .sorted(Comparator.comparing(ProductComponentMapping::getId))
                .map(this::toComponentResponse)
                .toList();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .originalPrice(product.getOriginalPrice())
                .price(product.getPrice())
                .recommended(product.isRecommended())
                .components(components)
                .build();
    }

    public ProductComponentResponse toComponentResponse(ProductComponentMapping mapping) {
        ProductComponent component = mapping.getComponent();

        return ProductComponentResponse.builder()
                .description(component.getDescription())
                .quantity(mapping.getQuantity())
                .build();
    }


    private int getViewTicketQuantity(Product product) {
        return product.getComponentMappings().stream()
                .filter(mapping -> mapping.getComponent().getComponentType() == ProductComponentType.VIEW_TICKET)
                .map(ProductComponentMapping::getQuantity)
                .findFirst()
                .orElse(0);
    }
}
