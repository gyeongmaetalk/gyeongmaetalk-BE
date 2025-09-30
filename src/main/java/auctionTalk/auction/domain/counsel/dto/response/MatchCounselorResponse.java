package auctionTalk.auction.domain.counsel.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchCounselorResponse {
    @Schema(description = "상담사 ID", example = "1")
    private Long counselorId;

    @Schema(description = "상담 신청 폼 ID", example = "1")
    private Long counselFormId;

    @Schema(description = "상담사 이름", example = "이정훈")
    private String counselorName;

    @Schema(description = "별점 평균", example = "4.6")
    private double score;

    @Schema(description = "리뷰 개수", example = "123")
    private int reviewCount;

    @Schema(description = "상담사 설명", example = "10년간 경매 실전 경험을 기반으로 실수 없는 낙찰을 도와 드립니다.")
    private String description;

    @Schema(description = "전문 분야", example = "아파트, 실거주 기능 분석, 지분경매, 권리분석")
    private String Specialization;

    @Schema(description = "경력", example = "10")
    private int experience;

    @Schema(description = "누적상담", example = "120")
    private int counselCount;
    
    @Schema(description = "자격증", example = "경매지도사 자격증")
    private String license;
}
