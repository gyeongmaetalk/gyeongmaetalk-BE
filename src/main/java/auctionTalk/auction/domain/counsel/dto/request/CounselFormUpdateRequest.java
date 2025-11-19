package auctionTalk.auction.domain.counsel.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CounselFormUpdateRequest {

    @NotBlank(message = "상담 목적은 필수 입력값입니다.")
    @Schema(description = "상담 목적", example = "투자 목적이에요.")
    private String purpose;

    @NotBlank(message = "관심 지역은 필수 입력값입니다.")
    @Schema(description = "관심 지역", example = "수도권.")
    private String area;

    @NotBlank(message = "희망하는 서비스 범위는 필수 입력값입니다.")
    @Schema(description = "서비스 범위", example = "서류 준비.")
    private String serviceType;

    @NotBlank(message = "궁금한 분야는 필수 입력값입니다.")
    @Schema(description = "궁금한 분야", example = "상가/빌딩 경매.")
    private String interest;

    @NotBlank(message = "경매 참여 명의 타입은 필수 입력값입니다.")
    @Schema(description = "경매 참여 명의", example = "개인")
    private String participantType;
}
