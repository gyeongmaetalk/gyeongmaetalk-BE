package auctionTalk.auction.domain.counsel.entity;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.common.BaseEntity;
import auctionTalk.auction.global.exception.CustomApiException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_counselor_date_time",
                        columnNames = {"counselor_id", "counsel_date", "counsel_time"}
                )
        },
        indexes = {
                // 리뷰 알림 배치(30분마다): 아직 안 보낸 지난 상담
                @Index(name = "idx_counsel_push", columnList = "push_sent, counsel_date"),
                // 상담 상태 갱신 스케줄러(5분마다): 상담 전 상태인 지난 상담
                @Index(name = "idx_counsel_status_date", columnList = "counsel_status, counsel_date")
        }
)
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

    @Enumerated(EnumType.STRING)
    private CounselStatus counselStatus;

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

    public void updateStatus(CounselStatus counselStatus) {
        this.counselStatus = counselStatus;
    }

    public void markSubscribed() {
        if (this.counselStatus == CounselStatus.SUBSCRIBE) {
            return;
        }
        this.counselStatus = CounselStatus.SUBSCRIBE;
    }
}