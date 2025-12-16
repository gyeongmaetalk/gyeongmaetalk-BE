package auctionTalk.auction.domain.counsel.service;

import auctionTalk.auction.domain.counsel.dto.request.AdminCounselSearchRequest;
import auctionTalk.auction.domain.counsel.dto.response.AdminCounselPagingResponse;
import auctionTalk.auction.domain.counsel.dto.response.AdminCounselResponse;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;

import java.time.LocalDate;
import java.util.List;

public interface AdminCounselService {

    AdminCounselPagingResponse<AdminCounselResponse> inquiryCounselsByCounselStatus(
            List<CounselStatus> statuses, LocalDate startDate, LocalDate endDate, int page, int size);
}
