package auctionTalk.auction.domain.counsel.repository;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CounselDslRepository {
    Page<Counsel> searchByConditions(
            List<CounselStatus> statuses,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
}
