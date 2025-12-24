package auctionTalk.auction.domain.counsel.scheduler;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselScheduler {

    private final CounselRepository counselRepository;

    @Scheduled(fixedDelay = 5 * 60 * 1000) // 5분 주기
    @Transactional
    public void reconcileCounselStatus() {

        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        // 이미 시간이 지난 상담만 조회
        List<Counsel> expiredCounsels =
                counselRepository.findExpiredCounsels(CounselStatus.COUNSEL_BEFORE, today, nowTime);

        for (Counsel counsel : expiredCounsels) {
            counsel.updateStatus(CounselStatus.COUNSEL_AFTER);
        }
    }
}