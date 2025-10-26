package auctionTalk.auction.domain.counselor.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CounselorCreateRequest {

    @Schema(description = "상담사 이름", example = "이정훈")
    private String name;

    @Schema(description = "전문 분야", example = "경매")
    private String Specialization;

    @Schema(description = "경력", example = "10")
    private Integer experience;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String cellPhone;

    @Schema(description = "전문가 소개", example = "친절한 상담 도와드립니다.")
    private String description;

    @Schema(description = "자격증", example = "경매지도사 자격증")
    private String license;
}
