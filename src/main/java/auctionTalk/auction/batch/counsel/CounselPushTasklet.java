package auctionTalk.auction.batch.counsel;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.fcm.service.FcmService;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.NotificationSetting;
import auctionTalk.auction.global.validation.ParamValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CounselPushTasklet implements Tasklet {

    private final CounselRepository counselRepository;
    private final FcmService notificationService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(40);

        List<Counsel> counsels = counselRepository.findAllForPush(threshold);

        for (Counsel counsel : counsels) {
            Member member = counsel.getMember();
            String token = member.getFcmToken();

            NotificationSetting setting = member.getNotificationSetting(); // 알림 설정 꺼놨을 시 패스
            if (!setting.isReviewNotificationEnabled()) {
                continue;
            }

            if (token == null || token.isBlank()) {
                log.warn("[PushSkip] fcmToken missing. memberId={}, counselId={}",
                        member.getId(), counsel.getId());
                continue;
            }

            notificationService.sendPushReviewNotification(
                    token,
                    "리뷰 작성",
                    "무료 상담은 어떠셨나요? 후기를 남겨주세요",
                    member,
                    counsel
            );

            counsel.updatePushSent();
        }

        return RepeatStatus.FINISHED;
    }
}
