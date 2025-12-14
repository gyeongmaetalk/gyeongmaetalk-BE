package auctionTalk.auction.domain.counsel.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminCounselResponse {

    @Schema(description = "상담 ID", example = "1")
    private Long counselId;

    @Schema(description = "고객 이름", example = "김영희")
    private String userName;

    @Schema(description = "고객 전화번호", example = "01026997463")
    private String userCellPhone;

    @Schema(description = "상담 날짜", example = "2021-12-31T12:00:00")
    private LocalDateTime counselDate;

    @Schema(description = "신청 날짜", example = "2021-12-31T12:00:00")
    private LocalDateTime applyDate;

    @Schema(description = "상담사 이름", example = "이정훈")
    private String counselorName;

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
