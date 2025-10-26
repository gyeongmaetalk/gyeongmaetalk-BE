package auctionTalk.auction.domain.fcm.repository;

import auctionTalk.auction.domain.fcm.entity.Notification;
import auctionTalk.auction.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByMemberAndCreatedAtAfterOrderByCreatedAtDesc(
            Member member,
            LocalDateTime thirtyDaysAgo
    );
}