package auctionTalk.auction.domain.subscription.entity;

import auctionTalk.auction.domain.counselor.entity.Counselor;
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
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private Counselor counselor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus subscriptionStatus;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    public void activate() {
        this.subscriptionStatus = SubscriptionStatus.IN_PROGRESS;
        this.startDate = LocalDateTime.now();
    }

    public void complete() {
        this.subscriptionStatus = SubscriptionStatus.COMPLETED;
        this.endDate = LocalDateTime.now();
    }

    public void failed() {
        this.subscriptionStatus = SubscriptionStatus.PAYMENT_FAILED;
    }
}