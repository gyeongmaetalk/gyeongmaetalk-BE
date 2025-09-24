package auctionTalk.auction.domain.member.service;


import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.entity.LoginType;

public interface AuthService {
    AuthTokenResponse login(String accessToken, LoginType provider);
    AuthTokenResponse refresh(String refreshToken);
}
