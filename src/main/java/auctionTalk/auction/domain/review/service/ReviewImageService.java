package auctionTalk.auction.domain.review.service;

import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.review.entity.ReviewImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewImageService {
    List<ReviewImage> createAndSaveReviewImages(Review review, List<MultipartFile> reviewImages);
    void deleteExistingImages(List<ReviewImage> imagesToRemove);
    void updateReviewImages(Review review, List<String> existingImageUrls, List<MultipartFile> newImages);
}
