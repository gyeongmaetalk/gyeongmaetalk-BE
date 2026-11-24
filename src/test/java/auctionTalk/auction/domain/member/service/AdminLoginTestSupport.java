package auctionTalk.auction.domain.member.service;

import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.mapper.AuthMapper;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.utils.sms.FakeStringRedis;
import org.springframework.security.crypto.password.PasswordEncoder;

final class AdminLoginTestSupport {
    static AdminAuthServiceImpl newService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                                           JwtTokenProvider jwtTokenProvider, AuthMapper authMapper) {
        return new AdminAuthServiceImpl(memberRepository, passwordEncoder, jwtTokenProvider, authMapper, new FakeStringRedis().template);
    }
}
