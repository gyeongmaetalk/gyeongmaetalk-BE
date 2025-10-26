package auctionTalk.auction.domain.counselor.service;

import auctionTalk.auction.domain.counselor.dto.request.CounselorCreateRequest;
import auctionTalk.auction.domain.counselor.dto.request.CounselorUpdateRequest;
import auctionTalk.auction.domain.counselor.dto.response.CounselorIdResponse;
import auctionTalk.auction.domain.counselor.dto.response.CounselorResponse;
import auctionTalk.auction.domain.member.entity.Member;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CounselorService {

    CounselorIdResponse createCounselor(CounselorCreateRequest counselorCreateRequest, List<MultipartFile> counselorImages);
    CounselorIdResponse updateCounselor(Long counselorId, CounselorUpdateRequest request, List<MultipartFile> newImages);

    CounselorResponse inquiryCounselor(Long counselorId, Member member);
}
