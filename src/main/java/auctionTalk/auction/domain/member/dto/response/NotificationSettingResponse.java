package auctionTalk.auction.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotificationSettingResponse {
    private boolean reviewNotificationEnabled;
    private boolean propertyNotificationEnabled;
}