package auctionTalk.auction.domain.counsel.mapper;

import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.dto.response.*;
import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselForm;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
                .counselStatus(CounselStatus.COUNSEL_BEFORE)
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

    public ApplyCounselResponse toApplyCounselResponse(Long counselId, CounselForm counselForm, LocalDate date, LocalTime time, Counselor counselor){
        return ApplyCounselResponse.builder()
                .counselId(counselId)
                .counselFormId(counselForm.getId())
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

    public CounselInfoResponse toCounselInfoResponse(Long counselId, Counselor counselor, CounselForm counselForm, LocalDate date, LocalTime time, boolean isReviewed, double averageScore, int reviewCount){
        return CounselInfoResponse.builder()
                .counselId(counselId)
                .counselorId(counselor.getId())
                .counselFormId(counselForm.getId())
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

    public AdminCounselResponse toAdminCounselResponse(Counsel counsel, Counselor counselor, LocalDateTime counselDate){

        Member member = counsel.getMember();
        CounselForm counselForm = counsel.getCounselForm();

        return AdminCounselResponse.builder()
                .counselId(counsel.getId())
                .userName(member.getName())
                .userCellPhone(member.getCellPhone())
                .counselDate(counselDate)
                .applyDate(counsel.getCreatedAt())
                .area(counselForm.getArea())
                .counselorName(counselor.getName())
                .cellPhone(counselor.getCellPhone())
                .interest(counselForm.getInterest())
                .purpose(counselForm.getPurpose())
                .participantType(counselForm.getParticipantType())
                .serviceType(counselForm.getServiceType())
                .counselStatus(counsel.getCounselStatus())
                .build();
    }

    public <T>AdminCounselPagingResponse<T> toAdminCounselPagingResponse(Page<T> counsels){

        return AdminCounselPagingResponse.<T>builder()
                .counsels(counsels.getContent())
                .page(counsels.getNumber())
                .totalPages(counsels.getTotalPages())
                .totalElements((int) counsels.getTotalElements())
                .isFirst(counsels.isFirst())
                .isLast(counsels.isLast())
                .build();
    }
}