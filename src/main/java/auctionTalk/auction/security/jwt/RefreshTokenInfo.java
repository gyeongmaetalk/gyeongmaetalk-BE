package auctionTalk.auction.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshTokenInfo {
    private Long memberId;
}