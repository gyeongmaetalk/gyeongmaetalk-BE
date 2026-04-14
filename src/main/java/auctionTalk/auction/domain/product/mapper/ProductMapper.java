package auctionTalk.auction.domain.product.mapper;

import auctionTalk.auction.domain.product.dto.response.ProductComponentResponse;
import auctionTalk.auction.domain.product.dto.response.ProductDetailResponse;
import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.domain.product.entity.ProductComponent;
import auctionTalk.auction.domain.product.entity.ProductComponentMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    public ProductDetailResponse toProductDetailResponse(Product product){
        List<ProductComponentResponse> components = product.getComponentMappings().stream()
                .sorted(Comparator.comparing(ProductComponentMapping::getId))
                .map(ProductComponentMapping::getComponent)
                .map(this::toComponentResponse)
                .toList();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .productType(product.getProductType().name())
                .price(product.getPrice())
                .components(components)
                .build();
    }

    public ProductComponentResponse toComponentResponse(ProductComponent component) {
        Integer ticketCount = null;

        if (component.getViewTicketDetail() != null) {
            ticketCount = component.getViewTicketDetail().getTicketCount();
        }

        return ProductComponentResponse.builder()
                .componentType(component.getComponentType().name())
                .name(component.getName())
                .description(component.getDescription())
                .ticketCount(ticketCount)
                .build();
    }
}
