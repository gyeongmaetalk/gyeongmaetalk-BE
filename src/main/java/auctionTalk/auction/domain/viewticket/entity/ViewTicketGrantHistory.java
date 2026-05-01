package auctionTalk.auction.domain.viewticket.entity;

import auctionTalk.auction.domain.order.entity.Order;
import auctionTalk.auction.domain.product.entity.ProductComponent;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "view_ticket_grant_histories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_view_ticket_grant_order_component",
                        columnNames = {"order_id", "product_component_id"}
                )
        }
)
public class ViewTicketGrantHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @JoinColumn(name = "product_component_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductComponent productComponent;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    private ViewTicketGrantHistory(Order order, ProductComponent productComponent, Integer quantity) {
        this.order = order;
        this.productComponent = productComponent;
        this.quantity = quantity;
    }
}