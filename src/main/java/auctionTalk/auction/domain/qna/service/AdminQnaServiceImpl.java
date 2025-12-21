package auctionTalk.auction.domain.qna.service;

import auctionTalk.auction.domain.qna.dto.response.AdminQnaInquiryResponse;
import auctionTalk.auction.domain.qna.dto.response.AdminQnaPagingResponse;
import auctionTalk.auction.domain.qna.entity.Qna;
import auctionTalk.auction.domain.qna.entity.QnaStatus;
import auctionTalk.auction.domain.qna.mapper.QnaMapper;
import auctionTalk.auction.domain.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AdminQnaServiceImpl implements AdminQnaService {

    private final QnaRepository qnaRepository;
    private final QnaMapper qnaMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminQnaPagingResponse<AdminQnaInquiryResponse> inquiryAdminQna(QnaStatus status, LocalDate startDate, LocalDate endDate, int size, int page){
        LocalDateTime startAt =
                (startDate == null) ? null : startDate.atStartOfDay();

        LocalDateTime endAt =
                (endDate == null) ? null : endDate.atTime(LocalTime.MAX);

        Page<Qna> qnaPage = qnaRepository.findAllByCondition(
                status,
                startAt,
                endAt,
                PageRequest.of(page, size)
        );
        return qnaMapper.toAdminQnaPagingResponse(
                qnaPage.map(qnaMapper::toAdminQnaInquiryResponse)
        );
    }
}
