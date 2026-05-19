package auctionTalk.auction.domain.member.entity;

import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at IS NULL")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clientId;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    private String name;

    private String cellPhone;

    private LocalDate birth;

    private String fcmToken;

    private String revenueCatAppUserId;

    @Column(nullable = false)
    private boolean registered = false;

    @Enumerated(EnumType.STRING)
    private Role role;
    
    // 어드민 계정 로그인용 아이디 패스워드
    private String username;
    private String password;

    @Embedded
    @Builder.Default
    private NotificationSetting notificationSetting = NotificationSetting.defaultSetting();

    public void completeRegistration(String name, LocalDate birth, String cellPhone) {
        this.name = name;
        this.birth = birth;
        this.cellPhone = cellPhone;
        this.registered = true;
    }

    @PrePersist
    protected void prePersist() {
        if (revenueCatAppUserId == null || revenueCatAppUserId.isBlank()) {
            revenueCatAppUserId = generateRevenueCatAppUserId();
        }
    }

    private String generateRevenueCatAppUserId() {
        return "rc_" + UUID.randomUUID();
    }

    public void saveFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateNotificationSetting(NotificationSetting newSetting) {
        this.notificationSetting.update(
                newSetting.isReviewNotificationEnabled(),
                newSetting.isPropertyNotificationEnabled()
        );
    }
}
