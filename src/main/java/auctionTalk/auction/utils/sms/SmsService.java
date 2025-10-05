package auctionTalk.auction.utils.sms;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsUtil smsUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "sms:code:";
    private static final long LIMIT_TIME = 5 * 60; //문자 인증 제한시간 (5분)

    public String SendSms(String phoneNumber) {
        String certificationCode = Integer.toString((int)(Math.random() * (999999 - 100000 + 1)) + 100000); // 6자리 인증 코드를 랜덤으로 생성

        redisTemplate.opsForValue().set(PREFIX + phoneNumber, certificationCode, LIMIT_TIME, TimeUnit.SECONDS);

        smsUtil.sendSMS(phoneNumber, certificationCode); // SMS 인증 유틸리티를 사용하여 SMS 발송

        return "문자를 성공적으로 발송했습니다.";
    }

    // 인증번호 검증
    public boolean verifyCode(String phoneNumber, String inputCode) {
        String key = PREFIX + phoneNumber;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) return false; // 만료 or 존재 안 함
        boolean result = savedCode.equals(inputCode);

        if (result) redisTemplate.delete(key); // 일회용: 성공 시 삭제

        return result;
    }
}
