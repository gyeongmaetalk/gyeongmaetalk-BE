package auctionTalk.auction.api.review.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewCreateRequestDto {

        @Schema(description = "상담 Id", example = "1")
        @NotNull(message = "상담 Id는 필수 입력 값입니다.")
        private Long counselId;

        @NotNull(message = "별점은 필수 입력 값입니다.")
        @Size(min=1, max=5, message = "별점은 1~5사이의 정수값 입니다.")
        private int score;

        @NotNull(message = "리뷰 내용은 필수 입력 값입니다.")
        @Size(max=250, message = "리뷰 내용은 250자 이하여야 합니다.")
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewUpdateRequestDto {

        @NotNull(message = "별점은 필수 입력 값입니다.")
        @Size(min=1, max=5, message = "별점은 1~5사이의 정수값 입니다.")
        private int score;

        @NotNull(message = "리뷰 내용은 필수 입력 값입니다.")
        @Size(max=250, message = "리뷰 내용은 250자 이하여야 합니다.")
        private String content;

        @Schema(description = "기존 이미지 URL 목록", example = "[\"s3 url1\", \"s3 url2\"]")
        private List<String> existingImages;
    }

    @Getter
    @AllArgsConstructor
    public static class ReviewIdResponseDto {
        private Long reviewId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewSummaryResponseDto {

        @Schema(description = "리뷰 ID", example = "1")
        private Long reviewId;

        @Schema(description = "사용자 이름", example = "dkd1234")
        private String name;

        @Schema(description = "리뷰 작성 날짜", example = "2022-12-31T12:00:00")
        private LocalDateTime createAt;

        @Schema(description = "상담 일자", example = "2021-12-31T12:00:00")
        private LocalDateTime counselDateTime;

        @Schema(description = "본인 게시글 여부", example = "true")
        private boolean isMine;

        @Schema(description = "별점", example = "4")
        private int score;

        @Schema(description = "리뷰 내용", example = "상담을 너무 자세히 해주셔서 좋았어요")
        private String content;

        @Schema(description = "리뷰 이미지 개수", example = "2")
        private int imageCount;

        @Schema(description = "리뷰 썸네일 URL", example = "thumbnail-url")
        private String thumbnail;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewDetailResponseDto{
        @Schema(description = "리뷰 ID", example = "1")
        private Long reviewId;

        @Schema(description = "사용자 이름", example = "dkd1234")
        private String name;

        @Schema(description = "리뷰 작성 날짜", example = "2022-12-31T12:00:00")
        private LocalDateTime createAt;

        @Schema(description = "상담 일자", example = "2021-12-31T12:00:00")
        private LocalDateTime counselDateTime;

        @Schema(description = "본인 게시글 여부", example = "true")
        private boolean isMine;

        @Schema(description = "별점", example = "4")
        private int score;

        @Schema(description = "리뷰 내용", example = "상담을 너무 자세히 해주셔서 좋았어요")
        private String content;

        @Schema(description = "이미지 URL 목록", example = "[\"url1\", \"url2\"]")
        private List<String> images;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewPagingResponseDto<T> {
        private List<T> reviews;
        private int page;
        private int totalPages;
        private int totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }
}
