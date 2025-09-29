package auctionTalk.auction.domain.member.service;


import auctionTalk.auction.domain.member.dto.request.SignupRequest;
import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.dto.response.MemberIdResponse;
import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;

public interface AuthService {
    AuthTokenResponse login(String accessToken, LoginType provider);
    MemberIdResponse register(Member member, SignupRequest request);
    AuthTokenResponse refresh(String refreshToken);
}
