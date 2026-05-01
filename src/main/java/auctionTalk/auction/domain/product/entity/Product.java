package auctionTalk.auction.domain.product.entity;

import auctionTalk.auction.domain.payment.entity.PaymentProvider;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeProductId;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    private Long originalPrice;

    private Long price;

    private boolean active;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProductComponentMapping> componentMappings = new ArrayList<>();
}
