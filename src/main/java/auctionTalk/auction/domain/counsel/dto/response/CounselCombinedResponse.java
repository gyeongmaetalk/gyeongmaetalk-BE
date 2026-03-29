package auctionTalk.auction.domain.counsel.dto.response;

import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CounselCombinedResponse {
    private CounselStatus status;
    private CounselInfoResponse info;
}