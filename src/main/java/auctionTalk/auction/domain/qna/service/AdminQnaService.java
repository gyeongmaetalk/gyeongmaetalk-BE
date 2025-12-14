package auctionTalk.auction.domain.qna.service;

import auctionTalk.auction.domain.qna.dto.response.AdminQnaInquiryResponse;
import auctionTalk.auction.domain.qna.dto.response.AdminQnaPagingResponse;

public interface AdminQnaService {

    AdminQnaPagingResponse<AdminQnaInquiryResponse> inquiryAdminQna(int page, int size);
}
