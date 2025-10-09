package auctionTalk.auction.domain.qna.repository;

import auctionTalk.auction.domain.qna.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findAllByOrderByCreatedAtDesc();
}
