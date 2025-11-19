package auctionTalk.auction.domain.counsel.entity;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private CounselForm counselForm;

    private LocalDate counselDate;

    private LocalTime counselTime;

    private boolean pushSent = false;

    public void updateCounsel(Member member, Counselor counselor, CounselForm counselForm,  LocalDate counselDate, LocalTime counselTime){
        this.member = member;
        this.counselor = counselor;
        this.counselForm = counselForm;
        this.counselDate = counselDate;
        this.counselTime = counselTime;
    }

    public void updatePushSent() {
        this.pushSent = true;
    }
}