package auctionTalk.auction.domain.review.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
import auctionTalk.auction.domain.review.dto.request.ReviewUpdateRequest;
import auctionTalk.auction.domain.review.dto.response.*;
import auctionTalk.auction.domain.review.entity.ReportType;
import auctionTalk.auction.domain.review.entity.ReviewSortType;
import auctionTalk.auction.domain.review.service.ReviewService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
            @AuthenticationPrincipal PrincipalDetails principal,
            @Parameter(description = "상담 후기 이미지 파일들(없을 시 사용 x)") @RequestPart(value = "reviewImages", required = false) List<MultipartFile> reviewImages,
            @Parameter(description = "상담 후기 생성 요청 json")  @Valid @RequestPart("request")ReviewCreateRequest request
            ){
        return BaseResponse.onSuccess(reviewService.createReview(request, reviewImages, principal.getMember()));
    }

    @Operation(summary = "상담 후기 수정 API")
    @PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<ReviewIdResponse> updateReview(
            @AuthenticationPrincipal PrincipalDetails member,
            @Parameter(description = "수정할 리뷰 id") @PathVariable Long reviewId,
            @Parameter(description = "상담 후기 이미지 파일들(없을 시 사용 x)") @RequestPart(value = "reviewImages", required = false) List<MultipartFile> reviewImages,
            @Parameter(description = "상담 후기 수정 요청 json")  @Valid @RequestPart("request") ReviewUpdateRequest request
    ){
        return BaseResponse.onSuccess(reviewService.updateReview(reviewId, request, reviewImages, member.getMember().getId()));
    }

    @Operation(summary = "상담 후기 삭제 API")
    @DeleteMapping(value = "/{reviewId}")
    public BaseResponse<ReviewIdResponse> deleteReview(
            @AuthenticationPrincipal PrincipalDetails member,
            @Parameter(description = "삭제할 리뷰 id") @PathVariable Long reviewId
    ){
        return BaseResponse.onSuccess(reviewService.deleteReview(reviewId, member.getMember().getId()));
    }

    @Operation(summary = "전체 후기 목록 조회 API")
    @GetMapping("/list")
    @Parameters(value = {
            @Parameter(name = "type", description = "리뷰 정렬 타입"),
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)"),
            @Parameter(name = "size", description = "한 페이지 당 이벤트 개수"),
    })
    public BaseResponse<AllReviewPagingResponse<ReviewSummaryResponse>> inquiryReviews(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam ReviewSortType type,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size
    ){
        Member member = principal != null ? principal.getMember() : null;
        return BaseResponse.onSuccess(reviewService.inquiryReviews(member, type, page, size));
    }

    @Operation(summary = "내가 쓴 후기 목록 조회 API")
    @GetMapping("/my")
    @Parameters(value = {
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)"),
            @Parameter(name = "size", description = "한 페이지 당 이벤트 개수"),
    })
    public BaseResponse<AllReviewPagingResponse<MyReviewSummaryResponse>> inquiryReviewsByMember(
            @AuthenticationPrincipal PrincipalDetails member,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size
    ){
        return BaseResponse.onSuccess(reviewService.inquiryReviewsByMember(member.getMember(), page, size));
    }

    @Operation(summary = "특정 상담사 후기 목록 조회 API")
    @GetMapping("/list/{counselorId}")
    @Parameters(value = {
            @Parameter(name = "counselorId", description = "상담사 id"),
            @Parameter(name = "type", description = "리뷰 정렬 타입"),
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)"),
            @Parameter(name = "size", description = "한 페이지 당 이벤트 개수"),
    })
    public BaseResponse<ReviewPagingResponse<ReviewSummaryResponse>> inquiryReviews(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable("counselorId") Long counselorId,
            @RequestParam ReviewSortType type,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size
    ){
        Member member = principal != null ? principal.getMember() : null;
        return BaseResponse.onSuccess(reviewService.inquiryReviewsByCounselor(counselorId, member, type, page, size));
    }

    @Operation(summary = "상담 후기 상세 조회 API")
    @GetMapping("/{reviewId}")
    public BaseResponse<ReviewDetailResponse> inquiryReview(
            @AuthenticationPrincipal PrincipalDetails member,
            @PathVariable("reviewId") Long reviewId
    ){
        return BaseResponse.onSuccess(reviewService.inquiryReviewDetail(reviewId, member.getMember()));
    }

    @Operation(summary = "리뷰 신고 API")
    @PostMapping("/{reviewId}/reports")
    @Parameters(value = {
            @Parameter(name = "reviewId", description = "신고할 리뷰 아이디"),
            @Parameter(name = "reasonType", description = "신고 이유 타입"),
            @Parameter(name = "reasonDetail", description = "신고 상세 이유"),
    })
    public BaseResponse<ReviewIdResponse> reportReview(
            @AuthenticationPrincipal PrincipalDetails member,
            @PathVariable("reviewId") Long reviewId,
            @RequestParam(name = "reasonType") ReportType reasonType,
            @RequestParam(name = "reasonDetail", required = false) String reasonDetail
            ){
        return BaseResponse.onSuccess(reviewService.reportReview(reviewId, member.getMember(), reasonType, reasonDetail));
    }

}
