package auctionTalk.auction.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingRequest {
    private boolean reviewNotificationEnabled;
    private boolean propertyNotificationEnabled;
}