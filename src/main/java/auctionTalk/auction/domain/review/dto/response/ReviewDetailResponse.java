package auctionTalk.auction.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class ReviewDetailResponse {

    @Schema(description = "리뷰 ID", example = "1")
    private Long reviewId;

    @Schema(description = "사용자 이름", example = "dkd1234")
    private String name;

    @Schema(description = "리뷰 작성 날짜", example = "2022-12-31T12:00:00")
    private LocalDateTime createAt;

    @Schema(description = "상담 날짜", example = "2021-12-31")
    private LocalDate counselDate;

    @Schema(description = "상담 시간", example = "12:00:00")
    private LocalTime counselTime;

    @Schema(description = "본인 게시글 여부", example = "true")
    private boolean isMine;

    @Schema(description = "별점", example = "4")
    private int score;

    @Schema(description = "리뷰 내용", example = "상담을 너무 자세히 해주셔서 좋았어요")
    private String content;

    @Schema(description = "이미지 URL 목록", example = "[\"url1\", \"url2\"]")
    private List<String> images;
}
