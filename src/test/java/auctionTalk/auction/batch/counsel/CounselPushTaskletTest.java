package auctionTalk.auction.batch.counsel;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.fcm.service.FcmService;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.NotificationSetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CounselPushTaskletTest {

    @InjectMocks
    private CounselPushTasklet counselPushTasklet;

    @Mock
    private CounselRepository counselRepository;

    @Mock
    private FcmService notificationService;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    private Member member;
    private NotificationSetting notificationSetting;
    private Counsel counsel;

    @BeforeEach
    void setUp() {
        notificationSetting = NotificationSetting.builder()
                .reviewNotificationEnabled(true)
                .propertyNotificationEnabled(true)
                .build();

        member = Member.builder()
                .id(1L)
                .clientId("test-client-id")
                .fcmToken("valid-fcm-token")
                .notificationSetting(notificationSetting)
                .build();

        counsel = Counsel.builder()
                .id(100L)
                .member(member)
                .build();
    }

    @Nested
    @DisplayName("리뷰 알림 배치 실행")
    class ExecuteTest {

        @Test
        @DisplayName("리뷰 알림 설정이 켜져 있고 FCM 토큰이 있으면 리뷰 푸시를 발송")
        void execute_success_sendPush() throws Exception {
            // given
            given(counselRepository.findAllForPush(any(LocalDateTime.class)))
                    .willReturn(List.of(counsel));

            // when
            RepeatStatus result = counselPushTasklet.execute(stepContribution, chunkContext);

            // then
            assertThat(result).isEqualTo(RepeatStatus.FINISHED);

            then(counselRepository).should().findAllForPush(any(LocalDateTime.class));
            then(notificationService).should().sendPushReviewNotification(
                    eq("valid-fcm-token"),
                    eq("리뷰 작성"),
                    eq("무료 상담은 어떠셨나요? 후기를 남겨주세요"),
                    eq(member),
                    eq(counsel)
            );
        }

        @Test
        @DisplayName("리뷰 알림 설정이 꺼져 있으면 푸시를 발송하지 않음")
        void execute_skip_whenReviewNotificationDisabled() throws Exception {
            // given
            notificationSetting.update(false,false);
            given(counselRepository.findAllForPush(any(LocalDateTime.class)))
                    .willReturn(List.of(counsel));

            // when
            RepeatStatus result = counselPushTasklet.execute(stepContribution, chunkContext);

            // then
            assertThat(result).isEqualTo(RepeatStatus.FINISHED);

            then(notificationService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("FCM 토큰이 없으면 푸시를 발송하지 않음")
        void execute_skip_whenTokenMissing() throws Exception {
            // given
            member.saveFcmToken(null);
            given(counselRepository.findAllForPush(any(LocalDateTime.class)))
                    .willReturn(List.of(counsel));

            // when
            RepeatStatus result = counselPushTasklet.execute(stepContribution, chunkContext);

            // then
            assertThat(result).isEqualTo(RepeatStatus.FINISHED);

            then(notificationService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("FCM 토큰이 빈 문자열이면 푸시를 발송하지 않음")
        void execute_skip_whenTokenBlank() throws Exception {
            // given
            member.saveFcmToken("   ");
            given(counselRepository.findAllForPush(any(LocalDateTime.class)))
                    .willReturn(List.of(counsel));

            // when
            RepeatStatus result = counselPushTasklet.execute(stepContribution, chunkContext);

            // then
            assertThat(result).isEqualTo(RepeatStatus.FINISHED);

            then(notificationService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("여러 상담 대상 중 발송 가능한 사용자에게만 푸시를 발송")
        void execute_sendOnlyEligibleMembers() throws Exception {
            // given
            NotificationSetting enabledSetting = NotificationSetting.builder()
                    .reviewNotificationEnabled(true)
                    .propertyNotificationEnabled(true)
                    .build();

            NotificationSetting disabledSetting = NotificationSetting.builder()
                    .reviewNotificationEnabled(false)
                    .propertyNotificationEnabled(true)
                    .build();

            Member enabledMember = Member.builder()
                    .id(1L)
                    .clientId("enabled")
                    .fcmToken("token-1")
                    .notificationSetting(enabledSetting)
                    .build();

            Member disabledMember = Member.builder()
                    .id(2L)
                    .clientId("disabled")
                    .fcmToken("token-2")
                    .notificationSetting(disabledSetting)
                    .build();

            Member noTokenMember = Member.builder()
                    .id(3L)
                    .clientId("no-token")
                    .fcmToken("")
                    .notificationSetting(enabledSetting)
                    .build();

            Counsel sendTarget = Counsel.builder().id(1L).member(enabledMember).build();
            Counsel disabledTarget = Counsel.builder().id(2L).member(disabledMember).build();
            Counsel noTokenTarget = Counsel.builder().id(3L).member(noTokenMember).build();

            given(counselRepository.findAllForPush(any(LocalDateTime.class)))
                    .willReturn(List.of(sendTarget, disabledTarget, noTokenTarget));

            // when
            RepeatStatus result = counselPushTasklet.execute(stepContribution, chunkContext);

            // then
            assertThat(result).isEqualTo(RepeatStatus.FINISHED);

            then(notificationService).should(times(1)).sendPushReviewNotification(
                    eq("token-1"),
                    eq("리뷰 작성"),
                    eq("무료 상담은 어떠셨나요? 후기를 남겨주세요"),
                    eq(enabledMember),
                    eq(sendTarget)
            );
        }
    }
}