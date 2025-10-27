package auctionTalk.auction.domain.fcm.entity;

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
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

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