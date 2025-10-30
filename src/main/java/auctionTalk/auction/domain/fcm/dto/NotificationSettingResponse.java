package auctionTalk.auction.domain.fcm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettingResponse {

    private boolean reviewNotificationEnabled;
    private boolean propertyNotificationEnabled;
}
