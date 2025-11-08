package auctionTalk.auction.domain.review.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
import auctionTalk.auction.domain.review.dto.request.ReviewUpdateRequest;
import auctionTalk.auction.domain.review.dto.response.*;
import auctionTalk.auction.domain.review.entity.ReportType;
import auctionTalk.auction.domain.review.entity.ReviewSortType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {

    //command
    ReviewIdResponse createReview(ReviewCreateRequest request, Member member);
    ReviewIdResponse updateReview(Long reviewId, ReviewUpdateRequest request, Long memberId);
    ReviewIdResponse deleteReview(Long reviewId, Long memberId);

    ReviewIdResponse reportReview(Long reviewId, Member member, ReportType reasonType, String reason);

    //query
    AllReviewPagingResponse<ReviewSummaryResponse> inquiryReviews(Member member, ReviewSortType sortType, int page, int size);
    AllReviewPagingResponse<MyReviewSummaryResponse> inquiryReviewsByMember(Member member, int page, int size);
    ReviewPagingResponse<ReviewSummaryResponse> inquiryReviewsByCounselor(Long counselorId, Member member, ReviewSortType sortType, int page, int size);
    ReviewDetailResponse inquiryReviewDetail(Long reviewId, Member member);
}
