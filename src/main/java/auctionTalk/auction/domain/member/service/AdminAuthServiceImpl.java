package auctionTalk.auction.domain.member.service;

import auctionTalk.auction.config.security.jwt.JwtToken;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.mapper.AuthMapper;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;

    @Override
    public AuthTokenResponse adminLogin(String username, String password) {
        Member admin = memberRepository.findByUsernameAndLoginType(username, LoginType.LOCAL)
                .orElseThrow(() -> new CustomApiException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new CustomApiException(ErrorCode.MEMBER_INVALID_PASSWORD);
        }

        JwtToken jwtToken = jwtTokenProvider.generateTokens(admin.getId(), admin.getRole().name());

        return authMapper.toAuthTokenResponse(admin.getId(), jwtToken);
    }
}
