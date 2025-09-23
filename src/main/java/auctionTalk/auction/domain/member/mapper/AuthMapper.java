package auctionTalk.auction.domain.member.mapper;

import auctionTalk.api.confing.security.jwt.JwtToken;
import auctionTalk.api.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.api.domain.member.entity.LoginType;
import auctionTalk.api.domain.member.entity.Member;
import auctionTalk.api.domain.member.entity.Role;
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
}
