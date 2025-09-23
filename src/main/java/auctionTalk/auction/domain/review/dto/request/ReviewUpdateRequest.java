package auctionTalk.auction.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewUpdateRequest {

    @NotNull(message = "별점은 필수 입력 값입니다.")
    @Size(min=1, max=5, message = "별점은 1~5사이의 정수값 입니다.")
    private int score;

    @NotNull(message = "리뷰 내용은 필수 입력 값입니다.")
    @Size(max=250, message = "리뷰 내용은 250자 이하여야 합니다.")
    private String content;

    @Schema(description = "기존 이미지 URL 목록", example = "[\"s3 url1\", \"s3 url2\"]")
    private List<String> existingImages;
}
