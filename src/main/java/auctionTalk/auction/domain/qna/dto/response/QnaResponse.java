package auctionTalk.auction.domain.qna.dto.response;

import auctionTalk.auction.domain.qna.entity.QnaCategory;
import auctionTalk.auction.domain.qna.entity.QnaStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QnaResponse {

    @Schema(description = "질문 아이디", example = "1")
    private Long id;

    @Schema(description = "질문 카테고리", example = "PAYMENT")
    private QnaCategory category;

    @Schema(description = "질문 제목", example = "상담 소요 시간 문의")
    private String qnaTitle;

    @Schema(description = "질문 내용", example = "얼마나 걸리나요?")
    private String qnaContent;

    @Schema(description = "질문 생성 시간", example = "2021-05-05T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "질문 상태", example = "PENDING")
    private QnaStatus qnaStatus;

    @Schema(description = "대답 내용", example = "얼마나 걸리게요~")
    private String answerContent;

    @Schema(description = "대답 시간", example = "2021-05-05T12:00:00")
    private LocalDateTime answerTime;
}
