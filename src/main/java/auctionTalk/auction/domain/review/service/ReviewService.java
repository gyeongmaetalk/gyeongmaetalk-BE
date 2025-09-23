package auctionTalk.auction.domain.review.service;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
import auctionTalk.auction.domain.review.dto.request.ReviewUpdateRequest;
import auctionTalk.auction.domain.review.dto.response.ReviewIdResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
    ReviewIdResponse createReview(ReviewCreateRequest request, List<MultipartFile> images, Member member);
    ReviewIdResponse updateReview(Long reviewId, ReviewUpdateRequest request, List<MultipartFile> newImages, Long memberId);
    ReviewIdResponse deleteReview(Long reviewId, Long memberId);
}
