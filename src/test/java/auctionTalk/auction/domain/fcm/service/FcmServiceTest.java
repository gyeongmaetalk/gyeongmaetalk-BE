package auctionTalk.auction.domain.fcm.service;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.fcm.dto.FcmTokenResponse;
import auctionTalk.auction.domain.fcm.dto.NotificationIdResponse;
import auctionTalk.auction.domain.fcm.dto.NotificationResponse;
import auctionTalk.auction.domain.fcm.entity.Notification;
import auctionTalk.auction.domain.fcm.entity.NotificationType;
import auctionTalk.auction.domain.fcm.mapper.FcmMapper;
import auctionTalk.auction.domain.fcm.mapper.NotificationMapper;
import auctionTalk.auction.domain.fcm.repository.NotificationRepository;
import auctionTalk.auction.domain.member.dto.response.NotificationSettingResponse;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.NotificationSetting;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {

    @InjectMocks
    private FcmService fcmService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private FcmMapper fcmMapper;

    @Mock
    private NotificationMapper notificationMapper;

    private Member member;
    private NotificationSetting notificationSetting;

    @BeforeEach
    void setUp() {
        notificationSetting = NotificationSetting.builder()
                .reviewNotificationEnabled(true)
                .propertyNotificationEnabled(true)
                .build();

        member = Member.builder()
                .id(1L)
                .clientId("test-client-id")
                .role(Role.USER)
                .notificationSetting(notificationSetting)
                .build();
    }

    @Nested
    @DisplayName("FCM 토큰 저장")
    class SaveFcmTokenTest {

        @Test
        @DisplayName("회원이 FCM 토큰을 저장하면 회원 정보에 토큰 반영")
        void saveFcmToken_success() {
            // given
            Long memberId = 1L;
            String fcmToken = "valid-fcm-token";

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.save(member)).willReturn(member);

            // when
            FcmTokenResponse result = fcmService.saveFcmToken(memberId, fcmToken);

            // then
            assertThat(result.getProfileId()).isEqualTo(memberId);
            assertThat(result.getFcmToken()).isEqualTo(fcmToken);
            assertThat(member.getFcmToken()).isEqualTo(fcmToken);

            then(memberRepository).should().findById(memberId);
            then(memberRepository).should().save(member);
        }

        @Test
        @DisplayName("존재하지 않는 회원의 FCM 토큰 저장 요청이 들어오면 예외가 발생")
        void saveFcmToken_fail_whenMemberNotFound() {
            // given
            Long invalidMemberId = 999L;
            String fcmToken = "valid-fcm-token";

            given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> fcmService.saveFcmToken(invalidMemberId, fcmToken))
                    .isInstanceOf(CustomApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

            then(memberRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("알림 목록 조회")
    class GetNotificationsTest {

        @Test
        @DisplayName("최근 30일 알림만 조회하고 알림 타입에 따라 다른 응답으로 매핑")
        void getNotifications_success_mapsByNotificationType() {
            // given
            Counselor counselor = Counselor.builder()
                    .id(10L)
                    .name("테스트 상담사")
                    .build();

            Counsel counsel = Counsel.builder()
                    .id(100L)
                    .counselor(counselor)
                    .counselDate(LocalDate.now().minusDays(1))
                    .counselTime(LocalTime.of(10, 0))
                    .build();

            Notification reviewNotification = Notification.builder()
                    .id(1L)
                    .member(member)
                    .title("리뷰를 남겨주세요")
                    .body("상담 후기를 작성해 주세요")
                    .type(NotificationType.COUNSEL_FINISHED)
                    .contentId(counselor.getId())
                    .counselorName(counselor.getName())
                    .counselTime(LocalDateTime.of(counsel.getCounselDate(), counsel.getCounselTime()))
                    .isRead(false)
                    .build();

            Notification propertyNotification = Notification.builder()
                    .id(2L)
                    .member(member)
                    .title("추천 매물")
                    .body("추천 매물이 등록되었어요")
                    .type(NotificationType.RECOMMENDED_PROPERTY)
                    .contentId(20L)
                    .propertyName("테스트 매물")
                    .thumbnail("thumbnail-url")
                    .counselorName("테스트 상담사")
                    .isRead(false)
                    .build();

            given(notificationRepository.findAllByMemberAndCreatedAtAfterOrderByCreatedAtDesc(eq(member), any(LocalDateTime.class)))
                    .willReturn(List.of(reviewNotification, propertyNotification));

            NotificationResponse reviewResponse = mock(NotificationResponse.class);
            NotificationResponse propertyResponse = mock(NotificationResponse.class);

            given(notificationMapper.toReviewNotification(reviewNotification)).willReturn(reviewResponse);
            given(notificationMapper.toPropertyNotification(propertyNotification)).willReturn(propertyResponse);

            // when
            List<?> result = fcmService.getNotifications(member);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).isEqualTo(reviewResponse);
            assertThat(result.get(1)).isEqualTo(propertyResponse);

            then(notificationMapper).should().toReviewNotification(reviewNotification);
            then(notificationMapper).should().toPropertyNotification(propertyNotification);
        }
    }

    @Nested
    @DisplayName("알림 읽음 처리")
    class ReadNotificationTest {

        @Test
        @DisplayName("본인 알림 읽기 요청이 들어오면 읽음 상태로 변경")
        void readNotification_success() {
            // given
            Long notificationId = 1L;

            Notification notification = Notification.builder()
                    .id(notificationId)
                    .member(member)
                    .title("추천 매물")
                    .body("추천 매물이 등록되었어요")
                    .type(NotificationType.RECOMMENDED_PROPERTY)
                    .isRead(false)
                    .build();

            given(notificationRepository.getNotification(notificationId)).willReturn(notification);
            given(notificationRepository.save(notification)).willReturn(notification);

            // when
            NotificationIdResponse result = fcmService.readNotification(member, notificationId);

            // then
            assertThat(result.getId()).isEqualTo(notificationId);
            assertThat(notification.isRead()).isTrue();

            then(notificationRepository).should().getNotification(notificationId);
            then(notificationRepository).should().save(notification);
        }
    }

    @Nested
    @DisplayName("알림 설정 조회")
    class GetNotificationSettingTest {

        @Test
        @DisplayName("회원의 알림 설정 조회 요청이 들어오면 현재 설정 정보를 반환")
        void getNotificationSetting_success() {
            // given
            NotificationSettingResponse response = NotificationSettingResponse.builder()
                    .reviewNotificationEnabled(true)
                    .propertyNotificationEnabled(true)
                    .build();

            given(notificationMapper.toNotificationSettingResponse(notificationSetting)).willReturn(response);

            // when
            NotificationSettingResponse result = fcmService.getNotificationSetting(member);

            // then
            assertThat(result).isEqualTo(response);
            then(notificationMapper).should().toNotificationSettingResponse(notificationSetting);
        }
    }
}