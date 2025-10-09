package auctionTalk.auction.domain.subscription.repository;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByMemberAndCounselorAndStatusIn(Member member, Counselor counselor, List<SubscriptionStatus> statuses);

    List<Subscription> findByMemberAndStatus(Member member, SubscriptionStatus status);

    default Subscription getSubscription(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
    }

    boolean existsByMember(Member member);
}
