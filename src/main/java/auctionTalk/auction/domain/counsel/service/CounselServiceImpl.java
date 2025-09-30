package auctionTalk.auction.domain.counsel.service;

import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.dto.response.ApplyCounselResponse;
import auctionTalk.auction.domain.counsel.dto.response.CounselIdResponse;
import auctionTalk.auction.domain.counsel.dto.response.MatchCounselorResponse;
import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselForm;
import auctionTalk.auction.domain.counsel.mapper.CounselMapper;
import auctionTalk.auction.domain.counsel.repository.CounselFormRepository;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselServiceImpl implements CounselService {

    private final CounselMapper counselMapper;
    private final CounselRepository counselRepository;
    private final CounselorRepository counselorRepository;
    private final CounselFormRepository counselFormRepository;

    @Override
    @Transactional
    public ApplyCounselResponse applyCounsel(Long counselFormId, Member member , Long counselorId, LocalDate counselDate, LocalTime counselTime){
        Counselor counselor = counselorRepository.getCounselor(counselorId);

        CounselForm counselForm = counselFormRepository.getCounselFormById(counselFormId);

        Counsel counsel = createAndSaveCounsel(member, counselor, counselDate, counselTime, counselForm);

        return counselMapper.toApplyCounselResponse(counselForm, counselDate, counselTime);
    }

    @Override
    @Transactional
    public MatchCounselorResponse matchCounselor(CounselFormCreateRequest request, Member member){
        Counselor counselor = counselorRepository.getCounselor(1L);

        CounselForm counselForm = createAndSaveCounselForm(request, member);

        return counselMapper.toMatchCounselorResponse(counselor, counselForm.getId());
    }

    @Override
    @Transactional
    public List<LocalTime> inquiryPossibleTime(Long counselorId, LocalDate date){
        Counselor counselor = counselorRepository.getCounselor(counselorId);

        List<LocalTime> possibleTimes = counselor.getAvailableTimeSlots();

        List<LocalTime> reservedTimes = counselRepository.findReservedTimes(counselorId, date);

        return possibleTimes.stream()
                .filter(time -> !reservedTimes.contains(time))
                .toList();
    }

    private Counsel createAndSaveCounsel(Member member, Counselor counselor, LocalDate counselDate, LocalTime counselTime, CounselForm counselForm){
        Counsel counsel = counselMapper.toCounsel(member, counselor, counselDate, counselTime, counselForm);

        return counselRepository.save(counsel);
    }

    private CounselForm createAndSaveCounselForm(CounselFormCreateRequest request, Member member){
        CounselForm newCounselForm = counselMapper.toCounselForm(request, member);

        return counselFormRepository.save(newCounselForm);
    }

}
