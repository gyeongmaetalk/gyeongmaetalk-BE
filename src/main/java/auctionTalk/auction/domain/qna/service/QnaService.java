package auctionTalk.auction.domain.qna.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.qna.dto.request.QnaCreateRequest;
import auctionTalk.auction.domain.qna.dto.response.FaqResponse;
import auctionTalk.auction.domain.qna.dto.response.QnaIdResponse;
import auctionTalk.auction.domain.qna.dto.response.QnaResponse;

import java.util.List;

public interface QnaService {

    QnaIdResponse createQna(QnaCreateRequest request, Member member);
    QnaIdResponse answerQna(Long qnaId, String content);
    List<QnaResponse> inquiryQnaByMember(Member member);
    List<FaqResponse> inquiryFaqList();
}
