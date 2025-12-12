package auctionTalk.auction.domain.member.service;

import auctionTalk.auction.config.security.jwt.JwtToken;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.dto.response.AuthTokenResponse;
import auctionTalk.auction.domain.member.dto.response.MemberIdResponse;
import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.domain.member.mapper.AuthMapper;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public MemberIdResponse createAdmin(String username, String password) {
        if (memberRepository.findByUsername(username).isPresent()) {
            throw new CustomApiException(ErrorCode.MEMBER_IS_PRESENT);
        }

        Member admin = Member.builder()
                .clientId(username)
                .username(username)
                .password(passwordEncoder.encode(password))
                .loginType(LoginType.LOCAL)
                .role(Role.ADMIN)
                .build();

        memberRepository.save(admin);

        return new  MemberIdResponse(admin.getId());
    }

    @Override
    @Transactional
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
