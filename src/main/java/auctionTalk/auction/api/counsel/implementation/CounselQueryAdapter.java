package auctionTalk.auction.api.counsel.implementation;

import auctionTalk.auction.api.counsel.domain.Counsel;
import auctionTalk.auction.api.counsel.persistence.CounselRepository;
import auctionTalk.auction.global.annotations.Adapter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Adapter
@RequiredArgsConstructor
public class CounselQueryAdapter {

    private final CounselRepository counselRepository;

    public Optional<Counsel> getCounselById(Long counselId) {
        return counselRepository.findById(counselId);
    }
}
