package auctionTalk.auction.domain.counsel.mapper;

import auctionTalk.auction.domain.counsel.dto.request.CounselFormCreateRequest;
import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselForm;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class CounselMapper {

    public Counsel toCounsel(Member member, Counselor counselor, LocalDate counselDate, LocalTime counselTime){
        return Counsel.builder()
                .member(member)
                .counselor(counselor)
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
}
