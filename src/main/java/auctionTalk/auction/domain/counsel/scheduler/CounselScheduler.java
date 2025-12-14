package auctionTalk.auction.domain.counsel.scheduler;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselScheduler {

    private final CounselRepository counselRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "0 0 * * * *") // 매 정각
    @Transactional
    public void reconcileCounselStatusHourly() {

        List<Counsel> targets =
                counselRepository.findAllByCounselStatusIn(
                        List.of(
                                CounselStatus.COUNSEL_BEFORE,
                                CounselStatus.SUBSCRIBE
                        )
                );

        for (Counsel counsel : targets) {
            CounselStatus newStatus =
                    calculateCounselStatus(counsel.getMember(), counsel);

            if (newStatus != counsel.getCounselStatus()) {
                counsel.updateStatus(newStatus);
            }
        }
    }

    private CounselStatus calculateCounselStatus(Member member, Counsel counsel) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime counselDateTime = LocalDateTime.of(
                counsel.getCounselDate(),
                counsel.getCounselTime()
        );

        if (counselDateTime.isAfter(now)) {
            return CounselStatus.COUNSEL_BEFORE;
        }

        boolean isSubscribed =
                subscriptionRepository.existsByMemberAndSubscriptionStatus(
                        member, SubscriptionStatus.IN_PROGRESS
                );

        return isSubscribed ? CounselStatus.SUBSCRIBE : CounselStatus.COUNSEL_AFTER;
    }
}