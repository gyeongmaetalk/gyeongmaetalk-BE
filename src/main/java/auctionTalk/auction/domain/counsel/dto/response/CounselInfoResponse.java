package auctionTalk.auction.domain.counsel.dto.response;

import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class CounselInfoResponse {

    @Schema(description = "상담사 ID", example = "1")
    private Long counselorId;

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
