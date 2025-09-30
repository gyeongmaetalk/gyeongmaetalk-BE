package auctionTalk.auction.domain.counsel.service;

import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.dto.response.CounselIdResponse;
import auctionTalk.auction.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CounselService {

    CounselIdResponse matchCounselor(CounselFormCreateRequest request, Member member);
    CounselIdResponse applyCounsel(Member member , Long counselorId, LocalDate counselDate, LocalTime counselTime);

    List<LocalTime> inquiryPossibleTime(Long counselId, LocalDate date);
}
