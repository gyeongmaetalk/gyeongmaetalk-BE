package auctionTalk.auction.domain.fcm.repository;

import auctionTalk.auction.domain.fcm.entity.Notification;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 엔티티를 하나씩 불러와 지우지 않고 DELETE 한 번으로 지운다(idx_notification_created_at).
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :threshold")
    int deleteAllCreatedBefore(@Param("threshold") LocalDateTime threshold);
    List<Notification> findAllByMemberAndCreatedAtAfterOrderByCreatedAtDesc(
            Member member,
            LocalDateTime thirtyDaysAgo
    );

    default Notification getNotification(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }
}