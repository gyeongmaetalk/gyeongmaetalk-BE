package auctionTalk.auction.domain.member.service;

import auctionTalk.auction.domain.member.dto.request.SignupRequest;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.utils.sms.SmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterTest {

    @Mock private MemberRepository memberRepository;
    @Mock private SmsService smsService;
    @InjectMocks private AuthServiceImpl authService;

    @Test
    @DisplayName("문자 인증 없이 전화번호를 등록할 수 없다 (기존: 요청 본문의 번호를 그대로 저장)")
    void unverifiedPhoneIsRejected() {
        Member member = Member.builder().build();

        assertThatThrownBy(() -> authService.register(member, new SignupRequest("홍길동", LocalDate.of(1990, 1, 1), "01099998888")))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SMS_NOT_VERIFIED);
        assertThat(member.getCellPhone()).isNull();
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("문자 인증을 마친 번호는 등록된다")
    void verifiedPhoneIsSaved() {
        Member member = Member.builder().id(7L).build();
        given(smsService.consumeVerifiedPhone(7L, "01099998888")).willReturn(true);

        authService.register(member, new SignupRequest("홍길동", LocalDate.of(1990, 1, 1), "01099998888"));

        assertThat(member.getCellPhone()).isEqualTo("01099998888");
        assertThat(member.isRegistered()).isTrue();
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("마이페이지에서 번호를 바꿀 때도 인증이 필요하다")
    void changingPhoneRequiresVerification() {
        Member member = Member.builder().id(7L).cellPhone("01012345678").build();

        assertThatThrownBy(() -> authService.register(member, new SignupRequest("홍길동", LocalDate.of(1990, 1, 1), "01055556666")))
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SMS_NOT_VERIFIED);
        assertThat(member.getCellPhone()).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("전화번호를 바꾸지 않는 정보 수정은 인증 없이 된다 (마이페이지 이름·생일 수정)")
    void unchangedPhoneNeedsNoVerification() {
        Member member = Member.builder().cellPhone("01012345678").build();

        authService.register(member, new SignupRequest("새이름", LocalDate.of(1990, 1, 1), "010-1234-5678"));

        assertThat(member.getName()).isEqualTo("새이름");
        verify(memberRepository).save(member);
    }
}
