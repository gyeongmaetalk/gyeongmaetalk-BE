package auctionTalk.auction.domain.member.service;

import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.dto.response.MemberIdResponse;

public interface AdminAuthService {
    MemberIdResponse createAdmin(String username, String password);
    AuthTokenResponse adminLogin(String username, String password);
}
