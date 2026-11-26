package auctionTalk.auction.utils.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmsServiceTest {

    private static final String PHONE = "01012345678";
    private static final long MEMBER_ID = 7L;

    private final SmsUtils smsUtils = mock(SmsUtils.class);
    private FakeStringRedis redis;
    private SmsService smsService;

    @BeforeEach
    void setUp() {
        redis = new FakeStringRedis();
        smsService = new SmsService(smsUtils, redis.template);
    }

    @Test
    @DisplayName("인증번호는 6자리 숫자다")
    void codeIsSixDigits() {
        assertThat(sendAndCapture()).matches("\\d{6}");
    }

    @Test
    @DisplayName("맞는 인증번호는 한 번만 통과한다")
    void correctCodeVerifiesOnce() {
        String code = sendAndCapture();

        assertThat(tryVerify(code)).isTrue();
        assertThat(tryVerify(code)).isFalse();
    }

    @Test
    @DisplayName("5번 틀리면 이후에는 맞는 번호를 넣어도 통과하지 못한다 (기존: 시도 횟수 제한 없이 100만 개 대입 가능)")
    void bruteForceIsBlocked() {
        String code = sendAndCapture();
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 5; i++) {
            assertThat(tryVerify(wrong)).isFalse();
        }

        assertThat(tryVerify(code)).isFalse();
    }

    @Test
    @DisplayName("같은 번호로 연달아 문자를 보낼 수 없다 (기존: 제한 없이 발송 = 문자 비용 폭탄)")
    void resendIsThrottled() {
        smsService.SendSms(PHONE);

        boolean secondSent;
        try {
            smsService.SendSms(PHONE);
            secondSent = true;
        } catch (RuntimeException e) {
            secondSent = false;
        }

        assertThat(secondSent).isFalse();
    }

    @Test
    @DisplayName("인증 성공 기록은 인증한 회원의 그 번호에만, 한 번만 쓸 수 있다")
    void verifiedPhoneIsBoundToMemberAndConsumedOnce() {
        assertThat(tryVerify(sendAndCapture())).isTrue();

        assertThat(smsService.consumeVerifiedPhone(MEMBER_ID + 1, PHONE)).as("다른 회원").isFalse();
        assertThat(smsService.consumeVerifiedPhone(MEMBER_ID, "01000000000")).as("다른 번호").isFalse();
        assertThat(smsService.consumeVerifiedPhone(MEMBER_ID, "010-1234-5678")).as("하이픈만 다른 같은 번호").isTrue();
        assertThat(smsService.consumeVerifiedPhone(MEMBER_ID, PHONE)).as("두 번째 사용").isFalse();
    }

    @Test
    @DisplayName("인증번호를 새로 받으면 틀린 횟수가 초기화된다")
    void resendResetsAttempts() {
        String first = sendAndCapture();
        String wrong = first.equals("000000") ? "111111" : "000000";
        for (int i = 0; i < 4; i++) {
            tryVerify(wrong);
        }

        redis.store.remove("sms:cooldown:" + PHONE); // 1분 대기를 건너뜀
        smsService.SendSms(PHONE);
        ArgumentCaptor<String> codes = ArgumentCaptor.forClass(String.class);
        verify(smsUtils, org.mockito.Mockito.times(2)).sendSMS(eq(PHONE), codes.capture());

        assertThat(tryVerify(codes.getAllValues().get(1))).isTrue();
    }

    private String sendAndCapture() {
        smsService.SendSms(PHONE);
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(smsUtils).sendSMS(eq(PHONE), code.capture());
        return code.getValue();
    }

    private boolean tryVerify(String code) {
        try {
            return smsService.verifyCode(MEMBER_ID, PHONE, code);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
