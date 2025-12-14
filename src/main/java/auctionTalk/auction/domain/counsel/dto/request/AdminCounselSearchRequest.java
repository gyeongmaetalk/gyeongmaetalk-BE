package auctionTalk.auction.domain.counsel.dto.request;

import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminCounselSearchRequest {

    private List<CounselStatus> statuses;
    private LocalDate startDate;
    private LocalDate endDate;
    private int page;
    private int size;
}

