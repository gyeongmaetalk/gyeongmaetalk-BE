package auctionTalk.auction.domain.member.service;

import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;

public interface AdminAuthService {
    AuthTokenResponse adminLogin(String username, String password);
}
