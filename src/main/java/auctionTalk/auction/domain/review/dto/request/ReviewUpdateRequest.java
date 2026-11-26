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

    @Schema(description = "현재 리뷰에 연결된 이미지의 S3 Object Key 목록 (URL 아님)", example = "[\"review/abc.webp\"]")
    private List<String> remainImageUrls;

    @Schema(description = "추가 업로드한 임시 S3 Object Key 목록 (URL 아님)",
            example = "[\"temp/review/1/550e8400-e29b-41d4-a716-446655440000.webp\"]")
    private List<String> addImageUrls;
}
