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
    private SubscriptionStatus status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String orderId;

    private String orderName;

    private Long amount;

    private String paymentKey;

    public void activate() {
        this.status = SubscriptionStatus.IN_PROGRESS;
        this.startDate = LocalDateTime.now();
    }

    public void complete() {
        this.status = SubscriptionStatus.COMPLETED;
        this.endDate = LocalDateTime.now();
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
        this.endDate = LocalDateTime.now();
    }
}