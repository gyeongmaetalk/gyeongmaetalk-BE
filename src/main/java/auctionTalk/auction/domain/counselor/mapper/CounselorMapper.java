package auctionTalk.auction.domain.counselor.mapper;

import auctionTalk.auction.domain.counselor.dto.request.CounselorCreateRequest;
import auctionTalk.auction.domain.counselor.dto.response.CounselorResponse;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.entity.CounselorImage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CounselorMapper {

    public Counselor toCounselor(CounselorCreateRequest request){
        return Counselor.builder()
                .name(request.getName())
                .Specialization(request.getSpecialization())
                .description(request.getDescription())
                .experience(request.getExperience())
                .cellPhone(request.getCellPhone())
                .license(request.getLicense())
                .build();
    }

    public CounselorImage toCounselorImage(Counselor counselor, String url){
        return CounselorImage.builder()
                .counselor(counselor)
                .url(url)
                .build();
    }

    public CounselorResponse toCounselorResponse(Counselor counselor, LocalDateTime counselDate){

        return CounselorResponse.builder()
                .name(counselor.getName())
                .experience(counselor.getExperience())
                .counselDate(counselDate)
                .build();
    }
}
