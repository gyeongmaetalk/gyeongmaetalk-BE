package auctionTalk.auction.domain.qna.service;

import auctionTalk.auction.domain.qna.dto.response.AdminQnaInquiryResponse;
import auctionTalk.auction.domain.qna.dto.response.AdminQnaPagingResponse;
import auctionTalk.auction.domain.qna.entity.QnaStatus;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

public interface AdminQnaService {

    AdminQnaPagingResponse<AdminQnaInquiryResponse> inquiryAdminQna(QnaStatus qnaStatus, LocalDate startDate, LocalDate endDate, int page, int size);
}
