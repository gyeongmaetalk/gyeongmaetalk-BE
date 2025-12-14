package auctionTalk.auction.domain.counsel.service;

import auctionTalk.auction.domain.counsel.dto.request.AdminCounselSearchRequest;
import auctionTalk.auction.domain.counsel.dto.response.AdminCounselPagingResponse;
import auctionTalk.auction.domain.counsel.dto.response.AdminCounselResponse;

public interface AdminCounselService {

    AdminCounselPagingResponse<AdminCounselResponse> inquiryCounselsByCounselStatus(AdminCounselSearchRequest request);
}
