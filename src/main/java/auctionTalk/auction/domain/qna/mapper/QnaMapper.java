package auctionTalk.auction.domain.qna.mapper;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.qna.dto.request.QnaCreateRequest;
import auctionTalk.auction.domain.qna.dto.response.FaqResponse;
import auctionTalk.auction.domain.qna.dto.response.QnaResponse;
import auctionTalk.auction.domain.qna.entity.Faq;
import auctionTalk.auction.domain.qna.entity.Qna;
import auctionTalk.auction.domain.qna.entity.QnaAnswer;
import auctionTalk.auction.domain.qna.entity.QnaStatus;
import org.springframework.stereotype.Component;

@Component
public class QnaMapper {

    public Qna ToQna(QnaCreateRequest request, Member member) {
        return Qna.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .status(QnaStatus.PENDING)
                .member(member)
                .build();
    }

    public QnaAnswer toQnaAnswer(String Content, Qna qna) {
        return QnaAnswer.builder()
                .qna(qna)
                .content(Content)
                .build();
    }

    public QnaResponse toQnaResponse(Qna qna) {
    QnaAnswer answer = qna.getAnswer();

    return QnaResponse.builder()
            .qnaTitle(qna.getTitle())
            .qnaContent(qna.getContent())
            .qnaStatus(qna.getStatus())
            .answerContent(answer != null ? answer.getContent() : null)
            .answerTime(answer != null ? answer.getCreatedAt() : null)
            .build();
}

    public FaqResponse toFaqResponse(Faq faq){
        return FaqResponse.builder()
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .build();
    }
}
