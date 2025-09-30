package auctionTalk.auction.domain.review.mapper;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
import auctionTalk.auction.domain.review.dto.response.ReviewDetailResponse;
import auctionTalk.auction.domain.review.dto.response.ReviewPagingResponse;
import auctionTalk.auction.domain.review.dto.response.ReviewSummaryResponse;
import auctionTalk.auction.domain.review.entity.ReportType;
import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.review.entity.ReviewImage;
import auctionTalk.auction.domain.review.entity.ReviewReport;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ReviewMapper {

    public Review toReview(ReviewCreateRequest request, Member member, Counsel counsel) {

        return Review.builder()
                .member(member)
                .counsel(counsel)
                .score(request.getScore())
                .content(request.getContent())
                .build();
    }
    
    public ReviewReport toReviewReport(ReportType reasonType, String reason, Member member, Review review){
        return ReviewReport.builder()
                .member(member)
                .reportType(reasonType)
                .content(reason)
                .review(review)
                .build();
    }

    public ReviewImage toReviewImage(Review review, String url) {
        return ReviewImage.builder()
                .review(review)
                .url(url)
                .build();
    }

    public ReviewSummaryResponse toReviewSummaryResponse(Review review, Member member) {
        return ReviewSummaryResponse.builder()
                .reviewId(review.getId())
                .score(review.getScore())
                .name(review.getMember().getName())
                .createAt(review.getCreatedAt())
                .counselDate(review.getCounsel().getCounselDate())
                .counselTime(review.getCounsel().getCounselTime())
                .isMine(Objects.equals(review.getMember().getId(), member.getId()))
                .imageCount(review.getImages().size())
                .thumbnail(review.getThumbnail())
                .content(review.getContent())
                .build();
    }

    public ReviewDetailResponse toReviewDetailResponse(Review review, Member member) {
        return ReviewDetailResponse.builder()
                .reviewId(review.getId())
                .score(review.getScore())
                .name(review.getMember().getName())
                .createAt(review.getCreatedAt())
                .counselDate(review.getCounsel().getCounselDate())
                .counselTime(review.getCounsel().getCounselTime())
                .isMine(Objects.equals(review.getMember().getId(), member.getId()))
                .images(toImageUrls(review.getImages()))
                .content(review.getContent())
                .build();
    }

    public <T> ReviewPagingResponse<T> toReviewPagingResponse(Page<T> reviews) {
        return ReviewPagingResponse.<T>builder()
                .reviews(reviews.getContent())
                .page(reviews.getNumber())
                .totalPages(reviews.getTotalPages())
                .totalElements((int) reviews.getTotalElements())
                .isFirst(reviews.isFirst())
                .isLast(reviews.isLast())
                .build();
    }

    private List<String> toImageUrls(List<ReviewImage> images) {
        return images.stream().map(ReviewImage::getUrl).toList();
    }
}
