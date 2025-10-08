package auctionTalk.auction.domain.review.service;

import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.review.entity.ReviewImage;
import auctionTalk.auction.domain.review.mapper.ReviewMapper;
import auctionTalk.auction.domain.review.repository.ReviewImageRepository;
import auctionTalk.auction.utils.S3FileComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewImageServiceImpl implements ReviewImageService {

    private final ReviewImageRepository reviewImageRepository;

    private final S3FileComponent s3FileComponent;

    private final ReviewMapper reviewMapper;

    /*
     * 리뷰 이미지 객체를 생성하고 DB에 저장
     */
    @Override
    public List<ReviewImage> createAndSaveReviewImages(Review review, List<MultipartFile> reviewImages) {
        return reviewImages.stream()
                .map(reviewImage -> s3FileComponent.uploadFile("review", reviewImage))
                .map(reviewUrl -> reviewMapper.toReviewImage(review, reviewUrl))
                .map(reviewImageRepository::save)
                .toList();
    }

    /*
     * 삭제할 기존 리뷰를 S3와 DB에서 삭제
     */
    @Override
    public void deleteExistingImages(List<ReviewImage> imagesToRemove) {
        for (ReviewImage image : imagesToRemove) {
            s3FileComponent.deleteFile(image.getUrl());
            reviewImageRepository.delete(image);
        }
    }

    /*
     * 리뷰 이미지를 업데이트
     */
    @Override
    @Transactional
    public void updateReviewImages(Review review, List<String> existingImageUrls, List<MultipartFile> newImages) {
        List<ReviewImage> existingImages = reviewImageRepository.findAllByReview(review);
        List<ReviewImage> existingImagesToKeep = existingImages.stream()
                .filter(image -> existingImageUrls.contains(image.getUrl()))
                .collect(Collectors.toList());

        List<ReviewImage> existingImagesToRemove = existingImages.stream()
                .filter(image -> !existingImageUrls.contains(image.getUrl()))
                .toList();

        // 새로운 이미지 추가
        List<ReviewImage> newReviewImages = (newImages != null)
                ? createAndSaveReviewImages(review, newImages) : List.of();

        // 유지할 기존 이미지와 새로운 이미지 병합 후 Review에 할당
        existingImagesToKeep.addAll(newReviewImages);
        review.changeImages(existingImagesToKeep);

        // 삭제할 기존 이미지 삭제
        deleteExistingImages(existingImagesToRemove);
    }
}
