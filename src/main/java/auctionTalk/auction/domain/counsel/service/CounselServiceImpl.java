package auctionTalk.auction.domain.counsel.service;

import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.dto.response.CounselIdResponse;
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
import java.time.LocalDateTime;
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
    public CounselIdResponse applyCounsel(Member member , Long counselorId, LocalDate counselDate, LocalTime counselTime){
        Counselor counselor = counselorRepository.getCounselor(counselorId);

        Counsel counsel = createAndSaveCounsel(member, counselor, counselDate, counselTime);

        return new CounselIdResponse(counsel.getId());
    }

    @Override
    @Transactional
    public CounselIdResponse matchCounselor(CounselFormCreateRequest request, Member member){
        CounselForm counselForm = createAndSaveCounselForm(request, member);

        return new CounselIdResponse(counselForm.getId());
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

    private Counsel createAndSaveCounsel(Member member, Counselor counselor, LocalDate counselDate, LocalTime counselTime){
        Counsel counsel = counselMapper.toCounsel(member, counselor, counselDate, counselTime);

        return counselRepository.save(counsel);
    }

    private CounselForm createAndSaveCounselForm(CounselFormCreateRequest request, Member member){
        CounselForm newCounselForm = counselMapper.toCounselForm(request, member);

        return counselFormRepository.save(newCounselForm);
    }

}
