package auctionTalk.auction.domain.fcm.entity;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        indexes = {
                // 내 알림 목록: 회원별 최근 30일을 최신순으로 읽는다
                @Index(name = "idx_notification_member_created", columnList = "member_id, created_at"),
                // 보관 기간이 지난 알림 삭제
                @Index(name = "idx_notification_created_at", columnList = "created_at")
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    // 알림 목록은 최근 30일만 보여주고, 그보다 오래된 알림은 매일 삭제한다.
    public static final int RETENTION_DAYS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    private Long contentId;
    private String title;
    private String body;
    private String counselorName;
    private boolean isRead;
    private String thumbnail;  // 추천 매물 썸네일
    private String propertyName;
    private LocalDateTime counselTime; // 상담 알림일 때만 사용

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    public void markAsRead() {
        this.isRead = true;
    }
}