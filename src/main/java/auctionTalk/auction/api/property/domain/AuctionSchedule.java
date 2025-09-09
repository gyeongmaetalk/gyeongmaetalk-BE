package auctionTalk.auction.api.property.domain;

import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionSchedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private Integer round;

    private Integer minPrice;

    private String result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Property property;
}
