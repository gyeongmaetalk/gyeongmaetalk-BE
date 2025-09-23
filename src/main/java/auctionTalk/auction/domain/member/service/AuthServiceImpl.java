package auctionTalk.auction.domain.member.service;

import auctionTalk.api.confing.security.jwt.JwtToken;
import auctionTalk.api.confing.security.jwt.JwtTokenProvider;
import auctionTalk.api.confing.security.jwt.RefreshTokenInfo;
import auctionTalk.api.domain.member.client.AppleMemberClient;
import auctionTalk.api.domain.member.client.KakaoMemberClient;
import auctionTalk.api.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.api.domain.member.entity.LoginType;
import auctionTalk.api.domain.member.entity.Member;
import auctionTalk.api.domain.member.mapper.AuthMapper;
import auctionTalk.api.domain.member.repository.MemberRepository;
import auctionTalk.api.global.exception.CustomApiException;
import auctionTalk.api.global.exception.ErrorCode;
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
