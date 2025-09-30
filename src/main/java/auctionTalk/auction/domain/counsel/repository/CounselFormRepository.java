package auctionTalk.auction.domain.counsel.repository;

import auctionTalk.auction.domain.counsel.entity.CounselForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounselFormRepository extends JpaRepository<CounselForm, Long> {
}
