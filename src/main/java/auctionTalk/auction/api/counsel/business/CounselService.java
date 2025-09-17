package auctionTalk.auction.api.counsel.business;

import auctionTalk.auction.api.counsel.domain.Counsel;
import auctionTalk.auction.api.counsel.implementation.CounselQueryAdapter;
import auctionTalk.auction.api.counsel.persistence.CounselRepository;
import auctionTalk.auction.global.common.exception.ThrowClass.CounselException;
import auctionTalk.auction.global.common.exception.base.GlobalErrorCode;
import auctionTalk.auction.global.common.exception.dto.ErrorResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CounselService {

    private final CounselQueryAdapter counselQueryAdapter;

    @Transactional
    public Counsel getCounsel(Long id){
        return counselQueryAdapter.getCounselById(id)
                .orElseThrow(() -> new CounselException(GlobalErrorCode.COUNSEL_NOT_FOUND));
    }
}
