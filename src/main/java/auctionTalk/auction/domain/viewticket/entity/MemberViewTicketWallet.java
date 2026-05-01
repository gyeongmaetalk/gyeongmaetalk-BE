package auctionTalk.auction.domain.viewticket.entity;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberViewTicketWallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    private Integer balance;

    public MemberViewTicketWallet(Member member, Integer balance) {
        this.member = member;
        this.balance = balance;
    }

    public void increase(int quantity) {
        this.balance += quantity;
    }

    public void decrease(int quantity) {
        if (this.balance < quantity) {
            throw new IllegalStateException("열람권이 부족합니다.");
        }
        this.balance -= quantity;
    }
}