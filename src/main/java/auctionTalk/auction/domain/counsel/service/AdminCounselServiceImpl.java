package auctionTalk.auction.domain.counsel.service;

import auctionTalk.auction.domain.counsel.dto.response.AdminCounselPagingResponse;
import auctionTalk.auction.domain.counsel.dto.response.AdminCounselResponse;
import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counsel.mapper.CounselMapper;
import auctionTalk.auction.domain.counsel.repository.CounselDslRepository;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCounselServiceImpl implements AdminCounselService {

    private final CounselDslRepository counselDslRepository;
    private final CounselMapper counselMapper;


    @Override
    @Transactional
    public AdminCounselPagingResponse<AdminCounselResponse> inquiryCounselsByCounselStatus(List<CounselStatus> statuses, LocalDate startDate, LocalDate endDate, int page, int size){
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Counsel> counselPage =
                counselDslRepository.searchByConditions(
                        statuses,
                        startDate,
                        endDate,
                        pageable
                );

        Page<AdminCounselResponse> responsePage =
                counselPage.map(counsel -> {

                    Counselor counselor = counsel.getCounselor();

                    // 상담 날짜 + 시간 합쳐서 LocalDateTime 생성
                    LocalDateTime counselDateTime = LocalDateTime.of(
                            counsel.getCounselDate(),
                            counsel.getCounselTime()
                    );

                    return counselMapper.toAdminCounselResponse(
                            counsel,
                            counselor,
                            counselDateTime
                    );
                });

        return counselMapper.toAdminCounselPagingResponse(responsePage);
    }
}
