package auctionTalk.auction.api.property.domain;

import auctionTalk.auction.api.counselor.domain.Counselor;
import auctionTalk.auction.api.user.domain.User;
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
public class Property extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Counselor counselor;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PropertyImage> images = new ArrayList<>();
}
