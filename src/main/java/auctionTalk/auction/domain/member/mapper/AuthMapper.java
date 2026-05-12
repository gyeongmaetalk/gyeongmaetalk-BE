package auctionTalk.auction.domain.member.mapper;

import auctionTalk.auction.config.security.jwt.JwtToken;
import auctionTalk.auction.domain.member.dto.request.NotificationSettingRequest;
import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.dto.response.MemberInfoResponse;
import auctionTalk.auction.domain.member.dto.response.NotificationSettingResponse;
import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.NotificationSetting;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public Member toMember(String clientId, LoginType loginType) {
        return Member.builder()
                .clientId(clientId)
                .loginType(loginType)
                .role(Role.USER)
                .build();
    }

    public AuthTokenResponse toAuthTokenResponse(
            Long memberId, JwtToken jwtToken) {
        return AuthTokenResponse.builder()
                .memberId(memberId)
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }

    public MemberInfoResponse toMemberInfoResponse(Member member, SubscriptionStatus auctionStatus){

        return MemberInfoResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .loginType(member.getLoginType())
                .cellPhone(member.getCellPhone())
                .birth(member.getBirth())
                .auctionStatus(auctionStatus)
                .revenueCatAppUserId(member.getRevenueCatAppUserId())
                .build();
    }

    public NotificationSettingResponse toNotificationSettingResponse(NotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .reviewNotificationEnabled(setting.isReviewNotificationEnabled())
                .propertyNotificationEnabled(setting.isPropertyNotificationEnabled())
                .build();
    }
}
