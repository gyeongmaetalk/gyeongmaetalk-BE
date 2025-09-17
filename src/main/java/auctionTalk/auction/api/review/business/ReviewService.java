package auctionTalk.auction.api.review.business;

import auctionTalk.auction.api.counsel.business.CounselService;
import auctionTalk.auction.api.counsel.domain.Counsel;
import auctionTalk.auction.api.review.domain.Review;
import auctionTalk.auction.api.review.domain.ReviewImage;
import auctionTalk.auction.api.review.domain.ReviewSortType;
import auctionTalk.auction.api.review.implementation.ReviewCommandAdapter;
import auctionTalk.auction.api.review.implementation.ReviewImageCommandAdapter;
import auctionTalk.auction.api.review.implementation.ReviewQueryAdapter;
import auctionTalk.auction.api.review.presentation.dto.ReviewDto;
import auctionTalk.auction.api.user.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewCommandAdapter reviewCommandAdapter;
    private final ReviewQueryAdapter reviewQueryAdapter;
    private final ReviewMapper reviewMapper;
    private final ReviewImageCommandAdapter reviewImageCommandAdapter;
    private final CounselService counselService;

    @Transactional
    public ReviewDto.ReviewIdResponseDto createReview(
            ReviewDto.ReviewCreateRequestDto requestDto, List<MultipartFile> reviewImages, User user
    ) {

        Counsel counsel = counselService.getCounsel(requestDto.getCounselId());

        Review newReview = reviewCommandAdapter.createReview(requestDto, user, counsel);

        if (reviewImages != null) {
            List<ReviewImage> newReviewImages = reviewImageCommandAdapter.createAndSaveReviewImage(newReview, reviewImages);
            newReview.changeImages(newReviewImages);
        }

        return new ReviewDto.ReviewIdResponseDto(newReview.getId());
    }

    @Transactional(readOnly = true)
    public ReviewDto.ReviewPagingResponseDto<ReviewDto.ReviewSummaryResponseDto> inquiryReviews(
            Long counselorId, ReviewSortType sortType, User user, int page, int size
    ){

        return reviewMapper.toReviewPagingResponseDto(
                reviewQueryAdapter.inquiryReviews(counselorId, user, sortType, PageRequest.of(page, size))
        );
    }

}
