package auctionTalk.auction.batch.review;

import auctionTalk.auction.domain.review.entity.ReviewImage;
import auctionTalk.auction.domain.review.repository.ReviewImageRepository;
import auctionTalk.auction.utils.s3.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewImageCleanupScheduler {

    private final ReviewImageRepository reviewImageRepository;
    private final S3Service s3Service;

    @Transactional
    @Scheduled(cron = "0 */10 * * * *")
    public void cleanupUnusedReviewImages() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);

        List<ReviewImage> targets = reviewImageRepository.findUnusedImagesBefore(cutoff);

        if (targets.isEmpty()) {
            return;
        }

        for (ReviewImage reviewImage : targets) {
            try {
                String key = reviewImage.getUrl();

                if (s3Service.existsFile(key)) {
                    s3Service.deleteFile(key);
                }

                reviewImageRepository.delete(reviewImage);

                log.info("미사용 리뷰 이미지 정리 완료. imageId={}, key={}",
                        reviewImage.getId(), key);

            } catch (Exception e) {
                log.warn("미사용 리뷰 이미지 정리 실패. imageId={}, key={}",
                        reviewImage.getId(), reviewImage.getUrl(), e);
            }
        }
    }
}