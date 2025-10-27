package auctionTalk.auction.domain.property.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyCreateRequest {

    @Schema(description = "이름", example = "서울 서초구 아파트")
    private String name;

    @Schema(description = "평수", example = "30")
    private Integer area;

    @Schema(description = "감정가", example = "1200000000")
    private Long appraisedPrice;

    @Schema(description = "최저입찰가", example = "960000000")
    private Long minPrice;

    @Schema(description = "소재지 주소", example = "서울특별시 서초구 서초대로 123")
    private String address;

    @Schema(description = "사건번호", example = "2025타경12345")
    private String caseNumber;

    @Schema(description = "사건명", example = "부동산 임의경매")
    private String caseTitle;

    @Schema(description = "법원명", example = "서울중앙지방법원")
    private String courtName;

    @Schema(description = "접수일", example = "2025-01-15")
    private LocalDate registrationDate;

    @Schema(description = "매물 상태", example = "매각 기일 예정")
    private String status;

    @Schema(description = "개시결정일", example = "2025-02-01")
    private LocalDate commencementDate;

    @Schema(description = "전문가 코멘트", example = "입지와 교통이 우수한 물건입니다.")
    private String expertComment;

    @Schema(description = "채무자", example = "홍길동")
    private String debtor;

    @Schema(description = "채권자", example = "국민은행")
    private String creditor;

    @Schema(description = "소유자", example = "김철수")
    private String owner;

    @Schema(description = "임차인", example = "이영희")
    private String tenant;

    @Schema(description = "건물 유형", example = "아파트")
    private String buildingType;
}