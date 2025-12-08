package auctionTalk.auction.domain.member.dto.response;

import auctionTalk.auction.config.security.jwt.JwtToken;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenResponse {
    private Long memberId;
    private String accessToken;
    private String refreshToken;

    public JwtToken toJwtToken() {
        return new JwtToken("Bearer", accessToken, refreshToken);
    }
}