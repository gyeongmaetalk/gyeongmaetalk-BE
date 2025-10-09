package auctionTalk.auction.domain.fcm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FcmTokenResponse {
    private Long profileId;
    private String fcmToken;
}