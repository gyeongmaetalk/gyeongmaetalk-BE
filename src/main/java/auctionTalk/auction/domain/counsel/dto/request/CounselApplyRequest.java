package auctionTalk.auction.domain.counsel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CounselApplyRequest {

    private CounselFormCreateRequest counselFormCreateRequest;

    @Schema(description = "상담 시간", example = "2025-12-30T12:00:00")
    private LocalDateTime counselTime;
}
