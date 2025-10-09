package auctionTalk.auction.domain.counsel.dto.response;

import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CounselStatusResponse {

    private CounselStatus counselStatus;
}
