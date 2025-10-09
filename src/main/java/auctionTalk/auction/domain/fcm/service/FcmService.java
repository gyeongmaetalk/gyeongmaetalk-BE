package auctionTalk.auction.domain.fcm.service;

import auctionTalk.auction.domain.fcm.dto.FcmTokenResponse;
import auctionTalk.auction.domain.fcm.mapper.FcmMapper;
import auctionTalk.auction.domain.member.entity.Member;

import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final MemberRepository memberRepository;

    private final FcmMapper fcmMapper;

    @DependsOn("firebaseConfig")
    public void sendPushNotification(String targetToken, String title, String body) {

        try {
            Message message = fcmMapper.ToMessage(targetToken, title, body);//fcm 메세지 객체 빌드
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new CustomApiException(ErrorCode.FIREBASE_PUSH_FAILED);
        }
    }

    @Transactional
    public FcmTokenResponse saveFcmToken(Long memberId, String fcmToken){
        Member member = memberRepository.findById(memberId).orElseThrow(()-> new CustomApiException(ErrorCode.MEMBER_NOT_FOUND));
        member.saveFcmToken(fcmToken);
        memberRepository.save(member);

        return new FcmTokenResponse(memberId, fcmToken);
    }
}
