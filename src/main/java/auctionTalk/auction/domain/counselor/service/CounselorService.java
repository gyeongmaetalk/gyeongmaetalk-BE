package auctionTalk.auction.domain.counselor.service;

import auctionTalk.auction.domain.counselor.dto.response.CounselorResponse;
import auctionTalk.auction.domain.member.entity.Member;

public interface CounselorService {

    CounselorResponse inquiryCounselor(Long counselorId, Member member);
}
