package auctionTalk.auction.api.counsel.persistence;

import auctionTalk.auction.api.counsel.domain.Counsel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounselRepository extends JpaRepository<Counsel, Long> {
}
