package auctionTalk.auction.domain.qna.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.qna.dto.request.QnaCreateRequest;
import auctionTalk.auction.domain.qna.dto.response.FaqResponse;
import auctionTalk.auction.domain.qna.dto.response.QnaIdResponse;
import auctionTalk.auction.domain.qna.dto.response.QnaResponse;
import auctionTalk.auction.domain.qna.entity.Faq;
import auctionTalk.auction.domain.qna.entity.Qna;
import auctionTalk.auction.domain.qna.entity.QnaAnswer;
import auctionTalk.auction.domain.qna.event.QnaCreatedEvent;
import auctionTalk.auction.domain.qna.mapper.QnaMapper;
import auctionTalk.auction.domain.qna.repository.FaqRepository;
import auctionTalk.auction.domain.qna.repository.QnaAnswerRepository;
import auctionTalk.auction.domain.qna.repository.QnaRepository;
import auctionTalk.auction.utils.n8n.N8nNotifyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaServiceImpl implements QnaService {

    private final QnaRepository qnaRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final QnaMapper qnaMapper;
    private final FaqRepository faqRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public QnaIdResponse createQna(QnaCreateRequest request, Member member) {
        Qna newQna = qnaMapper.ToQna(request, member);

        qnaRepository.save(newQna);

        applicationEventPublisher.publishEvent(
                new QnaCreatedEvent(
                        member.getName(),
                        newQna.getTitle(),
                        newQna.getContent()
                )
        );
        return new  QnaIdResponse(newQna.getId());
    }

    @Override
    @Transactional
    public QnaIdResponse answerQna(Long qnaId, String content) {
        Qna qna = qnaRepository.getQna(qnaId);

        QnaAnswer newAnswer = qnaMapper.toQnaAnswer(content, qna);

        qna.markAnswered(newAnswer);
        qnaAnswerRepository.save(newAnswer);

        return new QnaIdResponse(newAnswer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QnaResponse> inquiryQnaByMember(Member member){
        List<Qna> myQna = qnaRepository.findByMember(member);

        return myQna.stream().map(qnaMapper::toQnaResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqResponse> inquiryFaqList(){
        List<Faq> faqList = faqRepository.findAllByOrderByCreatedAtDesc();

        return faqList.stream().map(qnaMapper::toFaqResponse).toList();
    }
}
