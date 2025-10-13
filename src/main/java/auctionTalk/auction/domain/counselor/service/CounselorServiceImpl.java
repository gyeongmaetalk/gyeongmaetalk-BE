package auctionTalk.auction.domain.counselor.service;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.dto.response.CounselorResponse;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.mapper.CounselorMapper;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CounselorServiceImpl implements CounselorService {

    private final CounselorMapper counselorMapper;
    private final CounselorRepository counselorRepository;
    private final CounselRepository counselRepository;

    @Override
    @Transactional
    public CounselorResponse inquiryCounselor(Long counselorId, Member member){
        Counselor counselor = counselorRepository.getCounselor(counselorId);
        Counsel counsel = counselRepository.getCounselByMember(member);

        LocalDateTime counselDateTime = LocalDateTime.of(
                counsel.getCounselDate(),
                counsel.getCounselTime()
        );

        return counselorMapper.toCounselorResponse(counselor,counselDateTime);
    }
}
