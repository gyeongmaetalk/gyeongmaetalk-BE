package auctionTalk.auction.domain.counselor.repository;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.entity.CounselorImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CounselorImageRepository extends JpaRepository<CounselorImage, Long> {
    List<CounselorImage> findAllByCounselor(Counselor counselor);

}
