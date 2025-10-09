package auctionTalk.auction.domain.qna.repository;

import auctionTalk.auction.domain.qna.entity.QnaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {
}
