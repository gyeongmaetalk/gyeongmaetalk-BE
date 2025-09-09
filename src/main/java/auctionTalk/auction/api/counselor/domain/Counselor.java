package auctionTalk.auction.api.counselor.domain;

import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Counselor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String service;

    @Column(nullable = false)
    private String deed;

    @Column(nullable = false)
    private Integer experience;

    @Column(nullable = false)
    private String cellPhone;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String profileImage;
}
