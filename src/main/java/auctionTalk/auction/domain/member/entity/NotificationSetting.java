package auctionTalk.auction.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSetting {

    @Column(nullable = false)
    private boolean reviewNotificationEnabled; // 상담 후 리뷰 요청 알림

    @Column(nullable = false)
    private boolean propertyNotificationEnabled ; // 추천 매물 알림

    public void update(boolean reviewEnabled, boolean propertyEnabled) {
        this.reviewNotificationEnabled = reviewEnabled;
        this.propertyNotificationEnabled = propertyEnabled;
    }

    public static NotificationSetting defaultSetting() {
        return new NotificationSetting(true, true);
    }
}