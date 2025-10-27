package auctionTalk.auction.domain.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PropertyDetailResponse {

    @Schema(description = "매물 이름", example = "아이파크뷰")
    private String name;

    @Schema(description = "평수", example = "30")
    private Integer area;

    @Schema(description = "감정가", example = "320000000")
    private Long appraisedPrice;

    @Schema(description = "최저가", example = "240000000")
    private Long minPrice;

    @Schema(description = "주소", example = "서울 도봉구 창동 123-45, 101동 101호")
    private String address;

    @Schema(description = "사건번호", example = "2024타경12345")
    private String caseNumber;

    @Schema(description = "사건명", example = "부동산 임의경매")
    private String caseTitle;

    @Schema(description = "법원명", example = "서울북부지방법원")
    private String courtName;

    @Schema(description = "접수일", example = "2024-03-15")
    private LocalDate registrationDate;

    @Schema(description = "개시 결정일", example = "2024-08-02")
    private LocalDate commencementDate;

    @Schema(description = "현재 상태", example = "매각 기일 예정")
    private String status;

    @Schema(description = "입찰 일정 리스트")
    private List<ScheduleInfo> scheduleInfos;

    @Schema(description = "채무자", example = "풍천케미칼(주)")
    private String debtor;

    @Schema(description = "채권자", example = "에프에스비2409유동화전문 유한회사(양도인: 페퍼저축은행)")
    private String creditor;

    @Schema(description = "소유자", example = "이철수")
    private String owner;

    @Schema(description = "임차인", example = "김지은")
    private String tenant;

    @Schema(description = "구매 여부", example = "false")
    private boolean isPurchased;

    @Schema(description = "전문가 코멘트", example = "1회 유찰 후 80% 수준으로 진입 예정이며, 전세 시세 약 12억 전후로 예상됩니다.")
    private String expertComment;

    @Schema(description = "건물 유형", example = "아파트")
    private String buildingType;

    @Schema(description = "업데이트 날짜", example = "2025-03-15")
    private LocalDateTime updateDate;

    @Schema(description = "이미지 url 목록", example = "[\"url1\", \"url2\"]")
    private List<String> images;

    @Getter
    @Builder
    public static class ScheduleInfo {
        private int round;
        private LocalDate date;
        private Long price;
        private String result;
    }
}
