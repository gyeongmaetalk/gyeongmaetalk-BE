package auctionTalk.auction.domain.counsel.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class ApplyCounselResponse {

    @Schema(description = "상담 id", example = "1")
    private Long counselId;

    @Schema(description = "상담 신청폼 id", example = "1")
    private Long counselFormId;

    @Schema(description = "상담 날짜", example = "2021-12-31")
    private LocalDate counselDate;

    @Schema(description = "상담 시간", example = "12:00:00")
    private LocalTime counselTime;

    @Schema(description = "상담사 전화번호", example = "010-1234-5678")
    private String cellPhone;

    @Schema(description = "상담 목적", example = "실거주를 위한 집을 사고 싶어요.")
    private String purpose;

    @Schema(description = "지역", example = "수도권")
    private String area;

    @Schema(description = "희망 서비스", example = "낙찰부터 명도까지 전반적으로 도와주세요.")
    private String serviceType;

    @Schema(description = "궁금한 분야", example = "아파트 경매")
    private String interest;

    @Schema(description = "명의", example = "개인(감면 등의 목적을 이유로 개인 사업자를 고려중이에요.)")
    private String participantType;
}
