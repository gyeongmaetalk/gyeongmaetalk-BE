package auctionTalk.auction.domain.qna.service;

import auctionTalk.auction.domain.qna.dto.response.AdminQnaInquiryResponse;
import auctionTalk.auction.domain.qna.dto.response.AdminQnaPagingResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

public interface AdminQnaService {

    AdminQnaPagingResponse<AdminQnaInquiryResponse> inquiryAdminQna(LocalDate startDate, LocalDate endDate, int page, int size);
}
