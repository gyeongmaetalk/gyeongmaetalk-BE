package auctionTalk.auction.domain.member.service;


import auctionTalk.auction.domain.member.dto.request.NotificationSettingRequest;
import auctionTalk.auction.domain.member.dto.request.SignupRequest;
import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.dto.response.MemberIdResponse;
import auctionTalk.auction.domain.member.dto.response.MemberInfoResponse;
import auctionTalk.auction.domain.member.dto.response.NotificationSettingResponse;
import auctionTalk.auction.domain.member.entity.Member;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthTokenResponse login(Member member);
    void logout(HttpServletResponse response, Member member);
    void softDeleteMember(HttpServletResponse response, Member member);
    AuthTokenResponse exchangeCode(String code);
    MemberIdResponse register(Member member, SignupRequest request);
    AuthTokenResponse refresh(String refreshToken);
    MemberInfoResponse getMemberInfo(Member member);
    NotificationSettingResponse updateNotificationSetting(Member member, NotificationSettingRequest request);
}
