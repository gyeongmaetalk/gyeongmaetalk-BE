package auctionTalk.auction.utils.s3;

import auctionTalk.auction.global.exception.CustomApiException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class S3ServiceTest {
    private static final String TEMP = "temp/review/7/550e8400-e29b-41d4-a716-446655440000.webp";
    private static final String ACTIVE = "review/42/7/550e8400-e29b-41d4-a716-446655440000.webp";
    private S3Client client;
    private S3Presigner presigner;
    private S3Service service;

    @BeforeEach
    void setUp() {
        client = mock(S3Client.class);
        presigner = S3Presigner.builder().region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();
        service = new S3Service(presigner, client);
        ReflectionTestUtils.setField(service, "bucket", "test-bucket");
    }

    @AfterEach
    void close() {
        presigner.close();
        TransactionSynchronizationManager.clear();
    }

    @Test
    void reviewPutSignsTemporaryOwnedKeyWithoutWritingDbOrS3() {
        URI first = URI.create(service.generatePresignedPutUrl("review", "photo.webp", 7L));
        URI second = URI.create(service.generatePresignedPutUrl("review", "photo.webp", 7L));
        assertThat(first.getPath()).matches("/temp/review/7/[0-9a-f-]{36}\\.webp");
        assertThat(second.getPath()).isNotEqualTo(first.getPath());
        assertThat(first.getQuery()).containsPattern("X-Amz-Expires=(179|180)(?:&|$)").contains("content-type");
        verifyNoInteractions(client);
    }

    @Test
    void otherImageDomainsKeepTheirKeyFormat() {
        for (String category : List.of("property", "counselor")) {
            URI url = URI.create(service.generatePresignedPutUrl(category, "photo.webp", 7L));
            assertThat(url.getPath()).matches("/" + category + "/photo_[0-9a-f-]{36}\\.webp");
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"temp/review", "../review", "unknown", "review/7"})
    void rejectsUnapprovedUploadCategory(String category) {
        assertThatThrownBy(() -> service.generatePresignedPutUrl(category, "a.webp", 7L))
                .isInstanceOf(CustomApiException.class);
    }

    @Test
    void requiresAuthenticatedUploader() {
        assertThatThrownBy(() -> service.generatePresignedPutUrl("review", "a.webp", null))
                .isInstanceOf(CustomApiException.class);
    }

    @Test
    void propertyCannotPersistReviewTemporaryObjectsAsPermanentImages() {
        assertThatThrownBy(() -> service.validateNonReviewImages(List.of(TEMP)))
                .isInstanceOf(CustomApiException.class);
        assertThatCode(() -> service.validateNonReviewImages(List.of("property/legacy.webp")))
                .doesNotThrowAnyException();
        assertThatCode(() -> service.validateNonReviewImages(null)).doesNotThrowAnyException();
    }

    @Test
    void getStillSignsLegacyAndPermanentKeysForTenMinutes() {
        for (String key : List.of("review/legacy.webp", ACTIVE, "property/old.webp")) {
            URI url = URI.create(service.generatePresignedGetUrl(key));
            assertThat(url.getPath()).isEqualTo("/" + key);
            assertThat(url.getQuery()).containsPattern("X-Amz-Expires=(599|600)(?:&|$)");
        }
        assertThat(service.generatePresignedGetUrl(null)).isNull();
        verifyNoInteractions(client);
    }

    @Test
    void confirmationCopiesOutsideLifecycleAndLeavesSourceForLifecycle() {
        when(client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        assertThat(service.confirmReviewImage(TEMP, 7L, 42L, List.of())).isEqualTo(ACTIVE);
        verify(client).copyObject(CopyObjectRequest.builder().bucket("test-bucket")
                .copySource("test-bucket/" + TEMP).key(ACTIVE).build());
        verify(client, never()).deleteObject(any(DeleteObjectRequest.class));
        assertThat(ACTIVE).doesNotStartWith(S3Service.TEMP_REVIEW_PREFIX);
    }

    @Test
    void retryDoesNotOverwriteAnExistingDestinationEvenIfTempUploadWasReplayed() {
        when(client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build())
                .thenReturn(HeadObjectResponse.builder().build());
        assertThat(service.confirmReviewImage(TEMP, 7L, 42L, List.of())).isEqualTo(ACTIVE);
        assertThat(service.confirmReviewImage(TEMP, 7L, 42L, List.of())).isEqualTo(ACTIVE);
        verify(client, times(1)).copyObject(any(CopyObjectRequest.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"review/arbitrary.webp", "property/a.webp", "temp/review/7/a.webp",
            "temp/review/8/550e8400-e29b-41d4-a716-446655440000.webp",
            "temp/review/7/../a.webp", "https://bucket/temp/review/a.webp"})
    void invalidOrForeignKeysNeverReachS3(String key) {
        assertThatThrownBy(() -> service.confirmReviewImage(key, 7L, 42L, List.of()))
                .isInstanceOf(CustomApiException.class);
        verifyNoInteractions(client);
    }

    @Test
    void onlyPermanentKeysAlreadyAttachedToThisReviewCanBeRetained() {
        assertThat(service.confirmReviewImage(ACTIVE, 7L, 42L, List.of(ACTIVE))).isEqualTo(ACTIVE);
        assertThatThrownBy(() -> service.confirmReviewImage(ACTIVE, 7L, 99L, List.of()))
                .isInstanceOf(CustomApiException.class);
        assertThatThrownBy(() -> service.validateRetainedReviewImages(List.of("review/foreign.webp"), List.of(ACTIVE)))
                .isInstanceOf(CustomApiException.class);
        verifyNoInteractions(client);
    }

    @Test
    void missingSourceOrCopyFailureNeverReturnsAPermanentKey() {
        when(client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        when(client.copyObject(any(CopyObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        assertThatThrownBy(() -> service.confirmReviewImage(TEMP, 7L, 42L, List.of()))
                .isInstanceOf(NoSuchKeyException.class);
    }

    @Test
    void headPermissionFailureDoesNotTriggerCopy() {
        when(client.headObject(any(HeadObjectRequest.class))).thenThrow(S3Exception.builder().statusCode(403).build());
        assertThatThrownBy(() -> service.confirmReviewImage(TEMP, 7L, 42L, List.of()))
                .isInstanceOf(S3Exception.class);
        verify(client, never()).copyObject(any(CopyObjectRequest.class));
    }

    @Test
    void deletionRunsOnlyAfterCommitAndPartialS3ErrorsAreDetected() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        service.deleteFilesAfterCommit(List.of(ACTIVE));
        verifyNoInteractions(client);
        when(client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(DeleteObjectsResponse.builder()
                .errors(S3Error.builder().key(ACTIVE).code("AccessDenied").build()).build());
        assertThatThrownBy(() -> service.deleteFiles(List.of(ACTIVE))).isInstanceOf(IllegalStateException.class);
        // A cleanup error after commit is logged, not returned as a failed domain mutation.
        assertThatCode(() -> TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit))
                .doesNotThrowAnyException();
    }

    @Test
    void rollbackDoesNotDeleteExistingImages() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        service.deleteFilesAfterCommit(List.of(ACTIVE));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verifyNoInteractions(client);
    }
}
