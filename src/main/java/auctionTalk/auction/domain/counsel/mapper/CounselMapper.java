package auctionTalk.auction.domain.counsel.mapper;

import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.dto.response.ApplyCounselResponse;
import auctionTalk.auction.domain.counsel.dto.response.MatchCounselorResponse;
import auctionTalk.auction.domain.counsel.dto.response.CounselInfoResponse;
import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselForm;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class CounselMapper {

    public Counsel toCounsel(Member member, Counselor counselor, LocalDate counselDate, LocalTime counselTime, CounselForm counselForm){
        return Counsel.builder()
                .member(member)
                .counselor(counselor)
                .counselForm(counselForm)
                .counselDate(counselDate)
                .counselTime(counselTime)
                .build();
    }

    public CounselForm toCounselForm(CounselFormCreateRequest request, Member member){
        return CounselForm.builder()
                .member(member)
                .purpose(request.getPurpose())
                .area(request.getArea())
                .interest(request.getInterest())
                .serviceType(request.getServiceType())
                .participantType(request.getParticipantType())
                .build();
    }

    public MatchCounselorResponse toMatchCounselorResponse(Counselor counselor, Long counselFormId, double averageScore, int reviewCount){
        return MatchCounselorResponse.builder()
                .counselorId(counselor.getId())
                .counselFormId(counselFormId)
                .counselorName(counselor.getName())
                .score(averageScore)
                .counselCount(counselor.getCounselCount())
                .description(counselor.getDescription())
                .reviewCount(reviewCount)
                .experience(counselor.getExperience())
                .license(counselor.getLicense())
                .Specialization(counselor.getSpecialization())
                .build();
    }

    public ApplyCounselResponse toApplyCounselResponse(CounselForm counselForm, LocalDate date, LocalTime time, Counselor counselor){
        return ApplyCounselResponse.builder()
                .counselDate(date)
                .counselTime(time)
                .area(counselForm.getArea())
                .cellPhone(counselor.getCellPhone())
                .interest(counselForm.getInterest())
                .purpose(counselForm.getPurpose())
                .participantType(counselForm.getParticipantType())
                .serviceType(counselForm.getServiceType())
                .build();
    }

    public CounselInfoResponse toCounselInfoResponse(Counselor counselor, CounselForm counselForm, LocalDate date, LocalTime time, boolean isReviewed, double averageScore, int reviewCount){
        return CounselInfoResponse.builder()
                .counselorId(counselor.getId())
                .counselorName(counselor.getName())
                .score(averageScore)
                .counselCount(counselor.getCounselCount())
                .description(counselor.getDescription())
                .reviewCount(reviewCount)
                .experience(counselor.getExperience())
                .license(counselor.getLicense())
                .Specialization(counselor.getSpecialization())
                .counselDate(date)
                .counselTime(time)
                .area(counselForm.getArea())
                .cellPhone(counselor.getCellPhone())
                .interest(counselForm.getInterest())
                .purpose(counselForm.getPurpose())
                .participantType(counselForm.getParticipantType())
                .serviceType(counselForm.getServiceType())
                .isReviewed(isReviewed)
                .build();
    }
}
