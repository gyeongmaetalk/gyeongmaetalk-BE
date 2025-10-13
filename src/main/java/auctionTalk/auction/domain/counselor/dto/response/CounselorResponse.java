package auctionTalk.auction.domain.counselor.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CounselorResponse {

    @Schema(description = "상담사 이름", example = "이정훈")
    private String name;

    @Schema(description = "경력", example = "10")
    private int experience;

    @Schema(description = "상담 날짜", example = "2025-01-01T12:00:00")
    private LocalDateTime counselDate;
}