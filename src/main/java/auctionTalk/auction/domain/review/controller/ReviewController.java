package auctionTalk.auction.domain.review.controller;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
import auctionTalk.auction.domain.review.dto.request.ReviewUpdateRequest;
import auctionTalk.auction.domain.review.dto.response.ReviewIdResponse;
import auctionTalk.auction.domain.review.service.ReviewService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "상담 후기 API", description = "상담 후기 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "상담 후기 생성 API")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<ReviewIdResponse> createReview(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "상담 후기 이미지 파일들(없을 시 사용 x)") @RequestPart(value = "reviewImages", required = false) List<MultipartFile> reviewImages,
            @Parameter(description = "상담 후기 생성 요청 json")  @Valid @RequestPart("request")ReviewCreateRequest request
            ){
        return BaseResponse.onSuccess(reviewService.createReview(request, reviewImages, member));
    }

    @Operation(summary = "상담 후기 수정 API")
    @PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<ReviewIdResponse> updateReview(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "수정할 리뷰 id") @PathVariable Long reviewId,
            @Parameter(description = "상담 후기 이미지 파일들(없을 시 사용 x)") @RequestPart(value = "reviewImages", required = false) List<MultipartFile> reviewImages,
            @Parameter(description = "상담 후기 수정 요청 json")  @Valid @RequestPart("request") ReviewUpdateRequest request
    ){
        return BaseResponse.onSuccess(reviewService.updateReview(reviewId, request, reviewImages, member.getId()));
    }

    @Operation(summary = "상담 후기 삭제 API")
    @DeleteMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<ReviewIdResponse> deleteReview(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "삭제할 리뷰 id") @PathVariable Long reviewId
    ){
        return BaseResponse.onSuccess(reviewService.deleteReview(reviewId, member.getId()));
    }
}
