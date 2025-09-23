package auctionTalk.auction.member.service;

import auctionTalk.api.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.api.domain.member.entity.LoginType;

public interface AuthService {
    AuthTokenResponse login(String accessToken, LoginType provider);
    AuthTokenResponse refresh(String refreshToken);
}
