package auctionTalk.auction.domain.counsel.service;

import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.dto.response.ApplyCounselResponse;
import auctionTalk.auction.domain.counsel.dto.response.CounselStatusResponse;
import auctionTalk.auction.domain.counsel.dto.response.MatchCounselorResponse;
import auctionTalk.auction.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CounselService {

    MatchCounselorResponse matchCounselor(CounselFormCreateRequest request, Member member);
    ApplyCounselResponse applyCounsel(Long counselFormId, Member member , Long counselorId, LocalDate counselDate, LocalTime counselTime);

    List<LocalTime> inquiryPossibleTime(Long counselId, LocalDate date);
    CounselStatusResponse getCounselStatus(Member member);
}
