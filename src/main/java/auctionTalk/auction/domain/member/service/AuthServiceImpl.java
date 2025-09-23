package auctionTalk.auction.domain.member.service;

import auctionTalk.auction.config.security.jwt.JwtToken;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.config.security.jwt.RefreshTokenInfo;
import auctionTalk.auction.domain.member.client.AppleMemberClient;
import auctionTalk.auction.domain.member.client.KakaoMemberClient;
import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.mapper.AuthMapper;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final KakaoMemberClient kakaoMemberClient;
    private final AppleMemberClient appleMemberClient;

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public AuthTokenResponse login(String accessToken, LoginType loginType){
        String clientId = getClientIdByLoginType(accessToken, loginType);
        Optional<Member> member = memberRepository.findByClientIdAndLoginType(clientId, loginType);

        if(member.isPresent()){
            return generateTokensForExistingMember(member.get());
        }

        return generateTokensForNewMember(clientId, loginType);
    }

    @Override
    @Transactional
    public AuthTokenResponse refresh(String refreshToken) {
        RefreshTokenInfo refreshTokenInfo = jwtTokenProvider.validateAndExtractRefreshToken(refreshToken);

        Member member = memberRepository.getMember(refreshTokenInfo.getMemberId());
        return generateTokensForMember(member);
    }

    private String getClientIdByLoginType(String accessToken, LoginType loginType) {
        return switch (loginType) {
            case KAKAO -> kakaoMemberClient.getKakaoUserId(accessToken);
            case APPLE -> appleMemberClient.getAppleUserId(accessToken);
            default -> throw new CustomApiException(ErrorCode.INVALID_LOGIN_TYPE);
        };
    }

    private AuthTokenResponse generateTokensForExistingMember(final Member member) {
        JwtToken jwtToken = jwtTokenProvider.generateTokens(member.getId(), member.getRole().name());

        return authMapper.toAuthTokenResponse(member.getId(), jwtToken);
    }

    private AuthTokenResponse generateTokensForNewMember(final String clientId, final LoginType loginType) {
        Member newMember = memberRepository.save(authMapper.toMember(clientId, loginType));
        JwtToken jwtToken = jwtTokenProvider.generateTokens(
                newMember.getId(), newMember.getRole().name()
        );

        return authMapper.toAuthTokenResponse(newMember.getId(), jwtToken);
    }

    private AuthTokenResponse generateTokensForMember(final Member member) {
        JwtToken jwtToken = jwtTokenProvider.generateTokens(member.getId(), member.getRole().name());

        return authMapper.toAuthTokenResponse(member.getId(), jwtToken);
    }
}
