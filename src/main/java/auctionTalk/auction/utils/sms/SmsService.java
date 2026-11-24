package auctionTalk.auction.utils.sms;

import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;

/**
 * 휴대폰 문자 인증.
 * - 인증번호는 SecureRandom으로 만들고, 번호당 5번까지만 확인할 수 있다 (6자리 100만 개 대입 방지).
 * - 같은 번호로는 1분에 한 번, 하루 10번까지만 보낸다 (문자 비용 남용 방지).
 * - 인증에 성공하면 "이 회원이 이 번호를 인증했다"는 기록을 10분간 남기고, 회원가입·번호 변경 때 한 번 소비한다.
 */
@Service
@RequiredArgsConstructor
public class SmsService {

    private static final String CODE_PREFIX = "sms:code:";
    private static final String ATTEMPT_PREFIX = "sms:attempt:";
    private static final String COOLDOWN_PREFIX = "sms:cooldown:";
    private static final String DAILY_PREFIX = "sms:daily:";
    private static final String VERIFIED_PREFIX = "sms:verified:";

    private static final Duration CODE_TTL = Duration.ofMinutes(5);      // 인증번호 유효 시간
    private static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);
    private static final Duration DAILY_WINDOW = Duration.ofDays(1);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(10); // 인증 후 가입을 마칠 수 있는 시간
    private static final int DAILY_SEND_LIMIT = 10;
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SmsUtils smsUtils;
    private final StringRedisTemplate redisTemplate;

    public String SendSms(String phoneNumber) {
        String phone = normalizePhone(phoneNumber);

        Boolean firstInWindow = redisTemplate.opsForValue().setIfAbsent(COOLDOWN_PREFIX + phone, "1", RESEND_COOLDOWN);
        if (!Boolean.TRUE.equals(firstInWindow)) {
            throw new CustomApiException(ErrorCode.SMS_SEND_TOO_FREQUENT);
        }
        Long sentToday = redisTemplate.opsForValue().increment(DAILY_PREFIX + phone);
        if (sentToday != null && sentToday == 1) {
            redisTemplate.expire(DAILY_PREFIX + phone, DAILY_WINDOW);
        }
        if (sentToday != null && sentToday > DAILY_SEND_LIMIT) {
            throw new CustomApiException(ErrorCode.SMS_DAILY_LIMIT_EXCEEDED);
        }

        String certificationCode = String.format("%06d", RANDOM.nextInt(1_000_000));
        redisTemplate.opsForValue().set(CODE_PREFIX + phone, certificationCode, CODE_TTL);
        redisTemplate.delete(ATTEMPT_PREFIX + phone); // 새 번호를 받으면 시도 횟수 초기화

        smsUtils.sendSMS(phone, certificationCode);

        return "문자를 성공적으로 발송했습니다.";
    }

    /**
     * 인증번호 확인. 성공하면 memberId와 번호를 묶어 기록해 둔다.
     */
    public boolean verifyCode(Long memberId, String phoneNumber, String inputCode) {
        String phone = normalizePhone(phoneNumber);
        String codeKey = CODE_PREFIX + phone;
        String savedCode = redisTemplate.opsForValue().get(codeKey);
        if (savedCode == null || inputCode == null) {
            return false; // 만료 or 발송 안 함
        }

        Long attempts = redisTemplate.opsForValue().increment(ATTEMPT_PREFIX + phone);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(ATTEMPT_PREFIX + phone, CODE_TTL);
        }
        if (attempts != null && attempts > MAX_VERIFY_ATTEMPTS) {
            redisTemplate.delete(codeKey); // 다시 발송받아야 한다
            throw new CustomApiException(ErrorCode.SMS_VERIFY_LIMIT_EXCEEDED);
        }

        boolean matched = MessageDigest.isEqual(
                savedCode.getBytes(StandardCharsets.UTF_8), inputCode.getBytes(StandardCharsets.UTF_8));
        if (!matched) {
            return false;
        }

        redisTemplate.delete(codeKey); // 일회용
        redisTemplate.delete(ATTEMPT_PREFIX + phone);
        redisTemplate.opsForValue().set(VERIFIED_PREFIX + memberId, phone, VERIFIED_TTL);
        return true;
    }

    /**
     * 회원이 이 번호를 최근에 인증했는지 확인하고, 맞으면 기록을 지운다 (한 번만 사용).
     */
    public boolean consumeVerifiedPhone(Long memberId, String phoneNumber) {
        if (memberId == null) {
            return false;
        }
        String key = VERIFIED_PREFIX + memberId;
        String verifiedPhone = redisTemplate.opsForValue().get(key);
        if (verifiedPhone == null || !verifiedPhone.equals(normalizePhone(phoneNumber))) {
            return false;
        }
        redisTemplate.delete(key);
        return true;
    }

    /** 숫자만 남긴다 (010-1234-5678 → 01012345678) */
    public static String normalizePhone(String phoneNumber) {
        return phoneNumber == null ? null : phoneNumber.replaceAll("\\D", "");
    }
}
