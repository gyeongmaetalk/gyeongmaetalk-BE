package auctionTalk.auction.domain.qna.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FaqResponse {

    @Schema(description = "질문 아이디", example = "1")
    private Long id;

    @Schema(description = "질문", example = "상담은 얼마나 진행하나요?")
    private String question;

    @Schema(description = "대답", example = "30분 정도?")
    private String answer;
}
