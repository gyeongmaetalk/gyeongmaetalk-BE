package auctionTalk.auction.domain.fcm.mapper;

import auctionTalk.auction.domain.fcm.dto.NotificationResponse;
import auctionTalk.auction.domain.fcm.entity.Notification;
import auctionTalk.auction.domain.member.dto.response.NotificationSettingResponse;
import auctionTalk.auction.domain.member.entity.NotificationSetting;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toReviewNotification(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .contentId(notification.getContentId())
                .type(notification.getType())
                .counselorName(notification.getCounselorName())
                .isRead(notification.isRead())
                .thumbnail(null)
                .counselTime(notification.getCounselTime())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public NotificationResponse toPropertyNotification(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .contentId(notification.getContentId())
                .counselorName(notification.getCounselorName())
                .isRead(notification.isRead())
                .propertyName(notification.getPropertyName())
                .type(notification.getType())
                .thumbnail(notification.getThumbnail())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public NotificationSettingResponse toNotificationSettingResponse(NotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .reviewNotificationEnabled(setting.isReviewNotificationEnabled())
                .propertyNotificationEnabled(setting.isPropertyNotificationEnabled())
                .build();
    }
}
