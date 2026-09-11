package auctionTalk.auction.utils.s3;

import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
@Slf4j
public class S3Service {

    public static final String TEMP_REVIEW_PREFIX = "temp/review/";
    private static final Pattern TEMP_REVIEW_KEY = Pattern.compile(
            "^temp/review/[1-9][0-9]*/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.webp$");

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String generatePresignedPutUrl(String category, String originalFileName, Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new CustomApiException(ErrorCode.UNAUTHORIZED);
        }
        if (category == null || !List.of("review", "property", "counselor").contains(category)) {
            throw new CustomApiException(ErrorCode.BAD_REQUEST);
        }
        String fileName = "review".equals(category)
                ? TEMP_REVIEW_PREFIX + memberId + "/" + UUID.randomUUID() + ".webp"
                : createFileName(category, Objects.requireNonNull(originalFileName));

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType("image/webp")
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                builder -> builder
                        .signatureDuration(Duration.ofMinutes(3))
                        .putObjectRequest(putObjectRequest)
        );

        return presignedRequest.url().toString();
    }

    /**
     * Copy before the enclosing DB transaction commits. Never persist the temporary key.
     * The destination is specific to one review and is never overwritten on retry.
     * Keep the source for Lifecycle, including when the DB transaction rolls back.
     */
    public String confirmReviewImage(String key, Long memberId, Long reviewId, List<String> existingKeys) {
        if (key != null && !key.startsWith(TEMP_REVIEW_PREFIX) && existingKeys.contains(key)) {
            return key;
        }
        if (key == null || !TEMP_REVIEW_KEY.matcher(key).matches()
                || memberId == null || !key.startsWith(TEMP_REVIEW_PREFIX + memberId + "/")
                || reviewId == null || reviewId <= 0) {
            throw new CustomApiException(ErrorCode.BAD_REQUEST);
        }
        String destination = "review/" + reviewId + "/" + key.substring(TEMP_REVIEW_PREFIX.length());
        if (!existsFile(destination)) {
            // A missing source, denied copy or network error aborts the DB transaction.
            s3Client.copyObject(CopyObjectRequest.builder()
                    .copySource(bucket + "/" + key)
                    .bucket(bucket)
                    .key(destination)
                    .build());
        }
        return destination;
    }

    public void validateRetainedReviewImages(List<String> retainedKeys, List<String> existingKeys) {
        if (retainedKeys.stream().anyMatch(key -> key == null
                || key.startsWith(TEMP_REVIEW_PREFIX) || !existingKeys.contains(key))) {
            throw new CustomApiException(ErrorCode.BAD_REQUEST);
        }
    }

    public void validateNonReviewImages(List<String> keys) {
        if (keys != null && keys.stream().anyMatch(key -> key != null && key.startsWith(TEMP_REVIEW_PREFIX))) {
            throw new CustomApiException(ErrorCode.BAD_REQUEST);
        }
    }

    public void deleteFilesAfterCommit(List<String> keys) {
        if (keys.isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Image deletion requires a DB transaction");
        }
        List<String> deletedKeys = List.copyOf(keys);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    deleteFiles(deletedKeys);
                } catch (RuntimeException e) {
                    // A committed DB mutation must not be reported as a failed request.
                    log.error("Committed review image deletion needs retry. keys={}", deletedKeys, e);
                }
            }
        });
    }

    public String generatePresignedGetUrl(String fileKey) {
        if (!StringUtils.hasText(fileKey)) {
            return null;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                builder -> builder
                        .signatureDuration(Duration.ofMinutes(10))
                        .getObjectRequest(getObjectRequest)
        );

        return presignedRequest.url().toString();
    }

    public void deleteFile(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    public void deleteFiles(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> objects = keys.stream()
                .filter(StringUtils::hasText)
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        if (objects.isEmpty()) {
            return;
        }

        Delete delete = Delete.builder()
                .objects(objects)
                .build();

        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(delete)
                .build();

        DeleteObjectsResponse response = s3Client.deleteObjects(request);
        if (response.hasErrors()) {
            throw new IllegalStateException("S3 image deletion failed: " + response.errors());
        }
    }

    public boolean existsFile(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }


    /**
     * 파일명 생성
     * @param category
     * @param originalFileName
     * @return 작명된 파일 이름
     */
    public String createFileName(String category, String originalFileName) {
        int fileExtensionIndex = originalFileName.lastIndexOf(".");
        String fileExtension = originalFileName.substring(fileExtensionIndex);
        String fileName = originalFileName.substring(0, fileExtensionIndex);
        String random = String.valueOf(UUID.randomUUID());

        return category + "/" + fileName + "_" + random + fileExtension;
    }


}
