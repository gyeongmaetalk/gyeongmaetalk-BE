package auctionTalk.auction.domain.counselor.mapper;

import auctionTalk.auction.domain.counselor.dto.response.CounselorResponse;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CounselorMapper {

    public CounselorResponse toCounselorResponse(Counselor counselor, LocalDateTime counselDate){

        return CounselorResponse.builder()
                .name(counselor.getName())
                .experience(counselor.getExperience())
                .counselDate(counselDate)
                .build();
    }
}
