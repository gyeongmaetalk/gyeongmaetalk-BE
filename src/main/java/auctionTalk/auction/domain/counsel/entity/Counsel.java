package auctionTalk.auction.domain.counsel.entity;

import auctionTalk.auction.domain.counselor.entity.Area;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.entity.Field;
import auctionTalk.auction.domain.counselor.entity.ServiceType;
import auctionTalk.auction.domain.member.entity.Member;
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
    private Member member;

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