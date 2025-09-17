package auctionTalk.auction.api.review.presentation;

import auctionTalk.auction.api.review.business.ReviewService;
import auctionTalk.auction.api.review.domain.ReviewSortType;
import auctionTalk.auction.api.review.presentation.dto.ReviewDto;
import auctionTalk.auction.api.user.domain.User;
import auctionTalk.auction.global.common.CommonResponse;
import auctionTalk.auction.global.security.handler.annonation.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "상담 후기 API", description = "상담 후기 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewApi {

    private final ReviewService reviewService;

    @Operation(summary = "상담 후기 생성 API")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<ReviewDto.ReviewIdResponseDto> createReview(
            @AuthMember User user,
            @Parameter(description = "상담 후기 이미지 파일들(없을 시 사용 x)") @RequestPart(value = "reviewImages", required = false) List<MultipartFile> reviewImages,
            @Parameter(description = "상담 후기 생성 요청 json")  @Valid @RequestPart("request") ReviewDto.ReviewCreateRequestDto requestDto
    ){
        return CommonResponse.onSuccess(reviewService.createReview(requestDto, reviewImages, user));
    }

    @Operation(summary = "상당 후기 목록 조회 API")
    @GetMapping("/{counselorId}")
    @Parameters(value = {
            @Parameter(name = "counselorId", description = "상담사 id"),
            @Parameter(name = "type", description = "리뷰 정렬 타입"),
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)"),
            @Parameter(name = "size", description = "한 페이지 당 이벤트 개수"),
    })
    public CommonResponse<ReviewDto.ReviewPagingResponseDto<ReviewDto.ReviewSummaryResponseDto>> inquiryReviews(
            @AuthMember User user,
            @PathVariable("counselorId") Long counselorId,
            @RequestParam ReviewSortType type,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size
    ){
        return  CommonResponse.onSuccess(reviewService.inquiryReviews(counselorId, type, user, page, size));
    }

    @Operation(summary = "상담 후기 상세 조회 API")
    @GetMapping("/{reviewId}")
    public CommonResponse<ReviewDto.ReviewDetailResponseDto> inquiryReview(
            @AuthMember User user,
            @PathVariable("reviewId") Long reviewId
    ){
        return CommonResponse.onSuccess(reviewService.inquiryReview(reviewId, user));
    }

}
