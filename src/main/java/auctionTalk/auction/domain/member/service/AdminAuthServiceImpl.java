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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;
    private final StringRedisTemplate redisTemplate;

    // 관리자 아이디별로 15분 안에 5번 틀리면 15분간 로그인을 막는다
    private static final String LOGIN_FAIL_PREFIX = "admin:login:fail:";
    private static final int MAX_LOGIN_FAILURES = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private volatile String dummyHash;

    private String dummyHash() {
        if (dummyHash == null) {
            dummyHash = passwordEncoder.encode("dummy-password-for-timing");
        }
        return dummyHash;
    }

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
        String failKey = LOGIN_FAIL_PREFIX + username;
        String failures = redisTemplate.opsForValue().get(failKey);
        if (failures != null && Long.parseLong(failures) >= MAX_LOGIN_FAILURES) {
            throw new CustomApiException(ErrorCode.ADMIN_LOGIN_LOCKED);
        }

        // 없는 아이디도 BCrypt 비교를 한 번 해서, 응답 내용과 시간 모두로 아이디 존재 여부를 알 수 없게 한다
        Optional<Member> found = memberRepository.findByUsernameAndLoginType(username, LoginType.LOCAL);
        String passwordHash = found.map(Member::getPassword).orElseGet(this::dummyHash);
        boolean matched = passwordEncoder.matches(password, passwordHash) && found.isPresent();

        if (!matched) {
            Long count = redisTemplate.opsForValue().increment(failKey);
            if (count != null && count == 1) {
                redisTemplate.expire(failKey, LOCK_DURATION);
            }
            throw new CustomApiException(ErrorCode.ADMIN_LOGIN_FAILED);
        }
        redisTemplate.delete(failKey);
        Member admin = found.get();

        JwtToken jwtToken = jwtTokenProvider.generateTokens(admin.getId(), admin.getRole().name());

        return authMapper.toAuthTokenResponse(admin.getId(), jwtToken);
    }
}
