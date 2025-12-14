package auctionTalk.auction.domain.qna.service;

import auctionTalk.auction.domain.qna.dto.response.AdminQnaInquiryResponse;
import auctionTalk.auction.domain.qna.dto.response.AdminQnaPagingResponse;
import auctionTalk.auction.domain.qna.entity.Qna;
import auctionTalk.auction.domain.qna.mapper.QnaMapper;
import auctionTalk.auction.domain.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminQnaServiceImpl implements AdminQnaService {

    private final QnaRepository qnaRepository;
    private final QnaMapper qnaMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminQnaPagingResponse<AdminQnaInquiryResponse> inquiryAdminQna(int size, int page){
        Page<Qna> qnaPage =  qnaRepository.findAll(PageRequest.of(page, size));
        return qnaMapper.toAdminQnaPagingResponse(
                qnaPage.map(qnaMapper::toAdminQnaInquiryResponse)
        );
    }
}
