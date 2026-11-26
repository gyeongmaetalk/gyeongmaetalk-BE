package auctionTalk.auction.batch.review;

import auctionTalk.auction.domain.review.entity.ReviewImage;
import auctionTalk.auction.domain.review.repository.ReviewImageRepository;
import auctionTalk.auction.utils.s3.S3Service;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewImageCleanupSchedulerTest {
    private final ReviewImageRepository repository = mock(ReviewImageRepository.class);
    private final S3Service s3 = mock(S3Service.class);
    private final ReviewImageCleanupScheduler scheduler = new ReviewImageCleanupScheduler(repository, s3);

    @Test
    void lifecycleOwnsTemporaryObjectsWhileSchedulerOnlyRemovesTheirDbRows() {
        ReviewImage image = ReviewImage.builder().id(1L).url("temp/review/7/upload.webp").build();
        when(repository.findUnusedImagesBefore(any())).thenReturn(List.of(image));
        scheduler.cleanupUnusedReviewImages();
        verify(repository).delete(image);
        verifyNoInteractions(s3);
    }

    @Test
    void legacyRowsKeepTheirExistingS3ThenDbCleanup() {
        ReviewImage image = ReviewImage.builder().id(1L).url("review/old.webp").build();
        when(repository.findUnusedImagesBefore(any())).thenReturn(List.of(image));
        when(s3.existsFile(image.getUrl())).thenReturn(true);
        scheduler.cleanupUnusedReviewImages();
        var order = inOrder(s3, repository);
        order.verify(s3).existsFile(image.getUrl());
        order.verify(s3).deleteFile(image.getUrl());
        order.verify(repository).delete(image);
    }

    @Test
    void failedLegacyS3DeletionKeepsRowForRetry() {
        ReviewImage image = ReviewImage.builder().id(1L).url("review/old.webp").build();
        when(repository.findUnusedImagesBefore(any())).thenReturn(List.of(image));
        when(s3.existsFile(image.getUrl())).thenReturn(true);
        doThrow(new IllegalStateException("S3 unavailable")).when(s3).deleteFile(image.getUrl());
        scheduler.cleanupUnusedReviewImages();
        verify(repository, never()).delete(any());
    }
}
