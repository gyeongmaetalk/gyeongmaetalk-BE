package auctionTalk.auction.domain.review.controller;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
}
