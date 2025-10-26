package auctionTalk.auction.domain.fcm.service;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.fcm.dto.FcmTokenResponse;
import auctionTalk.auction.domain.fcm.dto.NotificationResponse;
import auctionTalk.auction.domain.fcm.entity.Notification;
import auctionTalk.auction.domain.fcm.entity.NotificationType;
import auctionTalk.auction.domain.fcm.mapper.FcmMapper;
import auctionTalk.auction.domain.fcm.mapper.NotificationMapper;
import auctionTalk.auction.domain.fcm.repository.NotificationRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final FcmMapper fcmMapper;
    private final NotificationMapper notificationMapper;

    @DependsOn("firebaseConfig")
    public void sendPushPropertyNotification(String targetToken, String title, String body, Member member, Property property) {

        try {
            Message message = fcmMapper.ToMessage(targetToken, title, body);//fcm 메세지 객체 빌드
            FirebaseMessaging.getInstance().send(message);

            Notification notification = Notification.builder()
                    .member(member)
                    .title(title)
                    .body(body)
                    .thumbnail(property.getThumbnail())
                    .counselorName(property.getCounselor().getName())
                    .propertyName(property.getName())
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        } catch (FirebaseMessagingException e) {
            throw new CustomApiException(ErrorCode.FIREBASE_PUSH_FAILED);
        }
    }

    @DependsOn("firebaseConfig")
    public void sendPushReviewNotification(String targetToken, String title, String body, Member member, Counsel counsel) {

        try {
            Message message = fcmMapper.ToMessage(targetToken, title, body);//fcm 메세지 객체 빌드
            FirebaseMessaging.getInstance().send(message);

            LocalDateTime counselDateTime = LocalDateTime.of(
                    counsel.getCounselDate(),
                    counsel.getCounselTime()
            );

            Notification notification = Notification.builder() // db에 저장할 엔티티 생성
                    .member(member)
                    .title(title)
                    .body(body)
                    .isRead(false)
                    .counselorName(counsel.getCounselor().getName())
                    .counselTime(counselDateTime)
                    .build();
            notificationRepository.save(notification);
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

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Member member) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<Notification> notifications =
                notificationRepository.findAllByMemberAndCreatedAtAfterOrderByCreatedAtDesc(member, thirtyDaysAgo);

        return notifications.stream()
                .map(notification -> {
                    if (notification.getType() == NotificationType.COUNSEL_FINISHED) {
                        return notificationMapper.toReviewNotification(notification);
                    }
                    if(notification.getType() == NotificationType.RECOMMENDED_PROPERTY){
                        return notificationMapper.toPropertyNotification(notification);
                    }
                    return null;
                })
                .toList();
    }
}
