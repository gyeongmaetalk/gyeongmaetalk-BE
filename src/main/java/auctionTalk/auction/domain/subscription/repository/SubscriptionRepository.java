package auctionTalk.auction.domain.subscription.repository;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByMemberAndCounselorAndSubscriptionStatusIn(Member member, Counselor counselor, List<SubscriptionStatus> statuses);

    List<Subscription> findByMemberAndSubscriptionStatus(Member member, SubscriptionStatus status);

    default Subscription getSubscription(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
    }

    boolean existsByMemberAndSubscriptionStatus(Member member, SubscriptionStatus status);
    Optional<Subscription> findByMember(Member member);

    Page<Subscription> findAllByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Subscription> findAllByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            LocalDateTime start, Pageable pageable);

    Page<Subscription> findAllByCreatedAtLessThanEqualOrderByCreatedAtDesc(
            LocalDateTime end, Pageable pageable);

    Page<Subscription> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
