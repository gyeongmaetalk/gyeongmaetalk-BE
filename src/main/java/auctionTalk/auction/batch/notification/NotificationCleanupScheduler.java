package auctionTalk.auction.batch.notification;

import auctionTalk.auction.domain.fcm.entity.Notification;
import auctionTalk.auction.domain.fcm.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 알림 목록은 최근 30일만 보여주는데 지난 알림을 지우는 곳이 없어 테이블이 계속 커졌다.
 * 매일 새벽에 보관 기간이 지난 알림을 지운다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private final NotificationRepository notificationRepository;

    @Transactional
    @Scheduled(cron = "0 0 4 * * *")
    public void deleteExpiredNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(Notification.RETENTION_DAYS);

        int deleted = notificationRepository.deleteAllCreatedBefore(threshold);

        if (deleted > 0) {
            log.info("보관 기간이 지난 알림 삭제. count={}, before={}", deleted, threshold);
        }
    }
}
