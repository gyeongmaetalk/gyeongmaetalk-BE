package auctionTalk.auction.domain.member.service;

import auctionTalk.auction.config.security.jwt.JwtToken;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.config.security.jwt.RefreshTokenInfo;
import auctionTalk.auction.domain.member.client.KakaoMemberClient;
import auctionTalk.auction.domain.member.dto.request.NotificationSettingRequest;
import auctionTalk.auction.domain.member.dto.request.SignupRequest;
import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.dto.response.MemberIdResponse;
import auctionTalk.auction.domain.member.dto.response.MemberInfoResponse;
import auctionTalk.auction.domain.member.dto.response.NotificationSettingResponse;
import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.mapper.AuthMapper;
import auctionTalk.auction.domain.member.repository.CodeRepository;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.domain.member.repository.TokenRepository;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.utils.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final KakaoMemberClient kakaoMemberClient;
    private final TokenRepository tokenRepository;
    private final MemberRepository memberRepository;
    private final CodeRepository codeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public AuthTokenResponse login(Member member){
        return generateTokensForExistingMember(member);
    }

    @Override
    @Transactional
    public void logout(HttpServletResponse response, Member member) {

        Long memberId = member.getId();

        // Refresh Token 삭제
        tokenRepository.deleteRefreshToken(memberId);

        // 쿠키 삭제
        CookieUtils.clearAuthCookies(response);

        // 인증 해제
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public AuthTokenResponse exchangeCode(String code){
        Long memberId = codeRepository.getMemberId(code);
        Member member = memberRepository.getMember(memberId);

        return login(member);
    }

    @Override
    @Transactional
    public MemberIdResponse register(Member member, SignupRequest request){
        member.completeRegistration(request.getName(), request.getBirth(), request.getCellPhone());

        memberRepository.save(member);

        return new MemberIdResponse(member.getId());
    }

    @Override
    @Transactional
    public AuthTokenResponse refresh(String refreshToken) {
        RefreshTokenInfo refreshTokenInfo = jwtTokenProvider.validateAndExtractRefreshToken(refreshToken);

        Member member = memberRepository.getMember(refreshTokenInfo.getMemberId());
        return generateTokensForMember(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberInfoResponse getMemberInfo(Member member){
        Subscription subscription = subscriptionRepository.findByMember(member)
                .orElse(null);

        SubscriptionStatus status =
                (subscription != null) ? subscription.getSubscriptionStatus() : null;

        return authMapper.toMemberInfoResponse(member, status);
    }

    @Override
    @Transactional
    public NotificationSettingResponse updateNotificationSetting(Member member, NotificationSettingRequest request){

        Member currentMember = memberRepository.getMember(member.getId());

        currentMember.getNotificationSetting().update(
                request.isReviewNotificationEnabled(),
                request.isPropertyNotificationEnabled()
        );

        return authMapper.toNotificationSettingResponse(currentMember.getNotificationSetting());
    }

    @Override
    @Transactional
    public void softDeleteMember(HttpServletResponse response, Member member){
        member.delete();
        tokenRepository.deleteRefreshToken(member.getId());
        CookieUtils.clearAuthCookies(response);
        SecurityContextHolder.clearContext();
    }

    private String getClientIdByLoginType(String accessToken, LoginType loginType) {
        return switch (loginType) {
            case KAKAO -> kakaoMemberClient.getKakaoUserId(accessToken);
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
