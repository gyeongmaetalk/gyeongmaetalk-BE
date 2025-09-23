package auctionTalk.auction.domain.review.service;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
import auctionTalk.auction.domain.review.dto.response.ReviewIdResponse;
import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.review.mapper.ReviewMapper;
import auctionTalk.auction.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl {

    private final CounselRepository counselRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewIdResponse createReview(
            ReviewCreateRequest request, List<MultipartFile> reviewImages, Member member
    ) {

        Counsel counsel = counselRepository.getCounselByMember(member);

        Review newReview = createAndSaveReview(request, member, counsel);

//        if (reviewImages != null) {
//            List<ReviewImage> newReviewImages = reviewImageCommandAdapter.createAndSaveReviewImage(newReview, reviewImages);
//            newReview.changeImages(newReviewImages);
//        }

        return new ReviewIdResponse(newReview.getId());
    }

    private Review createAndSaveReview(ReviewCreateRequest request, Member member, Counsel counsel) {
        Review newReview = reviewMapper.toReview(request, member, counsel);
        return reviewRepository.save(newReview);
    }
}
