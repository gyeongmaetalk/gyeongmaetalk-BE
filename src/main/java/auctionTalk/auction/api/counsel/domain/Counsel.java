package auctionTalk.auction.api.counsel.domain;

import auctionTalk.auction.api.counselor.domain.Area;
import auctionTalk.auction.api.counselor.domain.Counselor;
import auctionTalk.auction.api.counselor.domain.Field;
import auctionTalk.auction.api.counselor.domain.ServiceType;
import auctionTalk.auction.api.user.domain.User;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Counsel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Counselor counselor;

    private LocalDateTime bookedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ServiceType serviceType;
}
