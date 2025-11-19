package auctionTalk.auction.domain.member.entity;

import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private boolean registered = false;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Embedded
    @Builder.Default
    private NotificationSetting notificationSetting = NotificationSetting.defaultSetting();

    public void completeRegistration(String name, LocalDate birth, String cellPhone) {
        this.name = name;
        this.birth = birth;
        this.cellPhone = cellPhone;
        this.registered = true;
    }

    public void saveFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public void updateNotificationSetting(NotificationSetting newSetting) {
        this.notificationSetting.update(
                newSetting.isReviewNotificationEnabled(),
                newSetting.isPropertyNotificationEnabled()
        );
    }
}
