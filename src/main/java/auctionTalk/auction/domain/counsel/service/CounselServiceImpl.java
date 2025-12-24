package auctionTalk.auction.domain.counsel.service;

import auctionTalk.auction.domain.counsel.dto.request.CounselApplyRequest;
import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.dto.request.CounselFormUpdateRequest;
import auctionTalk.auction.domain.counsel.dto.response.*;
import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselForm;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counsel.mapper.CounselMapper;
import auctionTalk.auction.domain.counsel.repository.CounselFormRepository;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.review.repository.ReviewRepository;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import auctionTalk.auction.global.validation.ParamValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CounselServiceImpl implements CounselService {

    private final CounselMapper counselMapper;
    private final CounselRepository counselRepository;
    private final CounselorRepository counselorRepository;
    private final CounselFormRepository counselFormRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public MatchCounselorResponse matchCounselor(CounselFormCreateRequest request, Member member){

        boolean isSubscribed = subscriptionRepository
                .existsByMemberAndSubscriptionStatus(
                        member,
                        SubscriptionStatus.IN_PROGRESS
                );

        // 상담 신청 유효성 검사
        ParamValidator.validMatchCounsel(isSubscribed);

        Counselor counselor = counselorRepository.getCounselor(1L); // 상담사 고정

        List<Review> reviews = reviewRepository.findAllByCounsel_Counselor(counselor);
        int reviewCount = reviews.size();
        double averageScore = reviewCount > 0
                ? reviews.stream().mapToDouble(Review::getScore).average().orElse(0.0)
                : 0.0;

        return counselMapper.toMatchCounselorResponse(counselor, averageScore, reviewCount);
    }

    @Override
    @Transactional
    public CounselUpdateResponse updateCounselForm(Long counselFormId, CounselFormUpdateRequest request, Member member){
        Counselor counselor = counselorRepository.getCounselor(1L); // 상담사 고정

        CounselForm counselForm =  counselFormRepository.getCounselFormById(counselFormId);
        counselForm.updateCounselForm(request, member);

        counselFormRepository.save(counselForm);

        List<Review> reviews = reviewRepository.findAllByCounsel_Counselor(counselor);
        int reviewCount = reviews.size();
        double averageScore = reviewCount > 0
                ? reviews.stream().mapToDouble(Review::getScore).average().orElse(0.0)
                : 0.0;

        return counselMapper.toCounselUpdateResponse(counselor, counselForm.getId(), averageScore, reviewCount);
    }

    @Override
    @Transactional
    public ApplyCounselResponse applyCounsel(Member member, Long counselorId, CounselApplyRequest request){
        Counselor counselor = counselorRepository.getCounselor(counselorId);

        CounselForm counselForm = createAndSaveCounselForm(request.getCounselFormCreateRequest(), member);

        LocalDateTime counselDateTime = request.getCounselTime();

        Counsel counsel = createAndSaveCounsel(member, counselor, counselDateTime.toLocalDate(), counselDateTime.toLocalTime(), counselForm);

        counselor.addCounselCount();

        return counselMapper.toApplyCounselResponse(counsel.getId(), counselForm, counselDateTime.toLocalDate(), counselDateTime.toLocalTime(), counselor);
    }

    @Override
    @Transactional
    public ApplyCounselResponse updateApplyCounsel(Long counselId, Long counselFormId, Member member, Long counselorId, LocalDate counselDate, LocalTime counselTime){
        Counselor counselor = counselorRepository.getCounselor(counselorId);
        CounselForm counselForm = counselFormRepository.getCounselFormById(counselFormId);

        Counsel counsel = counselRepository.getCounselById(counselId);

        counsel.updateCounsel(member, counselor, counselForm, counselDate, counselTime);

        counselRepository.save(counsel);

        return counselMapper.toApplyCounselResponse(counsel.getId(), counselForm, counselDate, counselTime, counsel.getCounselor());
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

    @Override
    @Transactional
    public CounselCombinedResponse getCounselInfo(Member member){
        Optional<Counsel> counsel = counselRepository.findByMember(member);

        if (counsel.isEmpty()) {
            return new CounselCombinedResponse(CounselStatus.NONE, null);
        }

        Counsel existingCounsel = counsel.get();

        CounselForm counselForm = counselFormRepository.getCounselFormByMember(member);
        Counselor counselor = existingCounsel.getCounselor();

        List<Review> reviews = reviewRepository.findAllByCounsel_Counselor(counselor);
        int reviewCount = reviews.size();
        double averageScore = reviewCount > 0
                ? reviews.stream().mapToDouble(Review::getScore).average().orElse(0.0)
                : 0.0;

        boolean isReviewed = reviewRepository.existsByCounsel(existingCounsel);
        CounselInfoResponse info = counselMapper.toCounselInfoResponse(
                existingCounsel.getId(),
                counselor,
                counselForm,
                existingCounsel.getCounselDate(),
                existingCounsel.getCounselTime(),
                isReviewed,
                averageScore,
                reviewCount
        );
        return new CounselCombinedResponse(existingCounsel.getCounselStatus(), info);
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
