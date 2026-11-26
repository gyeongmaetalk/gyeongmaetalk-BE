package auctionTalk.auction.domain.member.service;

import auctionTalk.auction.config.security.jwt.JwtToken;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.domain.member.mapper.AuthMapper;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AdminLoginTest {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "correct-password";

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final AuthMapper authMapper = mock(AuthMapper.class);
    private AdminAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = AdminLoginTestSupport.newService(memberRepository, passwordEncoder, jwtTokenProvider, authMapper);

        Member admin = Member.builder().id(1L).username(USERNAME).password(passwordEncoder.encode(PASSWORD))
                .loginType(LoginType.LOCAL).role(Role.ADMIN).build();
        given(memberRepository.findByUsernameAndLoginType(anyString(), any(LoginType.class))).willReturn(Optional.empty());
        given(memberRepository.findByUsernameAndLoginType(USERNAME, LoginType.LOCAL)).willReturn(Optional.of(admin));
        given(jwtTokenProvider.generateTokens(anyLong(), anyString())).willReturn(new JwtToken("Bearer", "a", "r"));
    }

    @Test
    @DisplayName("없는 아이디와 틀린 비밀번호는 같은 에러로 응답한다 (기존: 응답만 보고 관리자 아이디 존재 여부를 알 수 있었음)")
    void sameErrorForUnknownUserAndWrongPassword() {
        ErrorCode unknownUser = catchThrowableOfType(() -> service.adminLogin("nobody", PASSWORD), CustomApiException.class).getErrorCode();
        ErrorCode wrongPassword = catchThrowableOfType(() -> service.adminLogin(USERNAME, "wrong"), CustomApiException.class).getErrorCode();

        assertThat(unknownUser).isEqualTo(wrongPassword).isEqualTo(ErrorCode.ADMIN_LOGIN_FAILED);
    }

    @Test
    @DisplayName("5번 틀리면 맞는 비밀번호로도 잠시 로그인할 수 없다 (기존: 무제한 대입 가능)")
    void lockedAfterFiveFailures() {
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> service.adminLogin(USERNAME, "wrong"));
        }

        assertThatThrownBy(() -> service.adminLogin(USERNAME, PASSWORD))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_LOGIN_LOCKED);
    }

    @Test
    @DisplayName("로그인에 성공하면 실패 횟수가 초기화된다")
    void successResetsFailures() {
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> service.adminLogin(USERNAME, "wrong"));
        }
        service.adminLogin(USERNAME, PASSWORD);

        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> service.adminLogin(USERNAME, "wrong"));
        }
        service.adminLogin(USERNAME, PASSWORD);
    }
}
