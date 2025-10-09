package auctionTalk.auction.domain.qna.repository;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.qna.entity.Qna;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaRepository extends JpaRepository<Qna, Long> {

    default Qna getQna(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.QNA_NOT_FOUND));
    }

    List<Qna> findByMember(Member member);
}
