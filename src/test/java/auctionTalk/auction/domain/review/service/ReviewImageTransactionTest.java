package auctionTalk.auction.domain.review.service;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.review.dto.request.ReviewCreateRequest;
import auctionTalk.auction.domain.review.dto.request.ReviewUpdateRequest;
import auctionTalk.auction.domain.review.entity.ReviewImage;
import auctionTalk.auction.domain.review.mapper.ReviewMapper;
import auctionTalk.auction.domain.review.repository.ReviewImageRepository;
import auctionTalk.auction.domain.review.repository.ReviewRepository;
import auctionTalk.auction.utils.s3.S3Service;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(ReviewImageTransactionTest.Config.class)
@TestPropertySource(properties = "cloud.aws.s3.bucket=test-bucket")
class ReviewImageTransactionTest {
    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {ReviewRepository.class, CounselRepository.class})
    @Import({ReviewServiceImpl.class, ReviewMapper.class})
    static class Config {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:reviewImages;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("auctionTalk.auction.domain");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop",
                    "hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"));
            return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
        @Bean CounselorRepository counselorRepository() { return mock(CounselorRepository.class); }
        @Bean S3Client s3Client() { return mock(S3Client.class); }
        @Bean S3Service s3Service(S3Client client) {
            return new S3Service(mock(S3Presigner.class), client);
        }
    }

    @Autowired ReviewService service;
    @Autowired ReviewRepository reviews;
    @Autowired ReviewImageRepository images;
    @Autowired S3Client s3;
    @Autowired PlatformTransactionManager transactionManager;
    @PersistenceContext EntityManager em;
    private TransactionTemplate tx;
    private Member member;
    private String temp;
    private final Set<String> objects = ConcurrentHashMap.newKeySet();

    @BeforeEach
    void setUp() {
        reset(s3);
        objects.clear();
        tx = new TransactionTemplate(transactionManager);
        member = tx.execute(status -> {
            Member result = Member.builder().clientId(UUID.randomUUID().toString()).build();
            em.persist(result);
            em.persist(Counsel.builder().member(result).build());
            return result;
        });
        temp = "temp/review/" + member.getId() + "/550e8400-e29b-41d4-a716-446655440000.webp";
        objects.add(temp);
        when(s3.headObject(any(HeadObjectRequest.class))).thenAnswer(invocation -> {
            String key = invocation.<HeadObjectRequest>getArgument(0).key();
            if (!objects.contains(key)) throw NoSuchKeyException.builder().build();
            return HeadObjectResponse.builder().build();
        });
        when(s3.copyObject(any(CopyObjectRequest.class))).thenAnswer(invocation -> {
            CopyObjectRequest request = invocation.getArgument(0);
            if (!objects.contains(request.copySource().substring("test-bucket/".length())))
                throw NoSuchKeyException.builder().build();
            objects.add(request.key());
            return CopyObjectResponse.builder().build();
        });
        when(s3.deleteObject(any(DeleteObjectRequest.class))).thenAnswer(invocation -> {
            objects.remove(invocation.<DeleteObjectRequest>getArgument(0).key());
            return DeleteObjectResponse.builder().build();
        });
    }

    private ReviewCreateRequest createRequest() {
        return ReviewCreateRequest.builder().score(5).content("review").imageUrls(List.of(temp)).build();
    }

    @Test
    void successfulCreateCommitsPermanentKeyAndDeletesTempOnlyAtCompletion() {
        tx.executeWithoutResult(status -> {
            service.createReview(createRequest(), member);
            assertThat(objects).contains(temp).hasSize(2);
            verify(s3, never()).deleteObject(any(DeleteObjectRequest.class));
        });
        String permanent = tx.execute(status -> {
            List<ReviewImage> saved = images.findAllByReview(em.createQuery(
                    "select r from Review r where r.member.id = :id", auctionTalk.auction.domain.review.entity.Review.class)
                    .setParameter("id", member.getId()).getSingleResult());
            assertThat(saved).hasSize(1);
            return saved.get(0).getUrl();
        });
        assertThat(permanent).startsWith("review/");
        assertThat(objects).containsExactly(permanent);
        verify(s3).deleteObject(DeleteObjectRequest.builder().bucket("test-bucket").key(temp).build());
    }

    @Test
    void copyFailureRollsBackReviewAndImageRows() {
        when(s3.copyObject(any(CopyObjectRequest.class))).thenThrow(S3Exception.builder().statusCode(503).build());
        assertThatThrownBy(() -> service.createReview(createRequest(), member)).isInstanceOf(S3Exception.class);
        assertNoReview();
        assertThat(objects).containsExactly(temp);
    }

    @Test
    void dbRollbackAfterCopyDeletesOnlyTheNewPermanentCopy() {
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            service.createReview(createRequest(), member);
            throw new IllegalStateException("DB transaction failed after copy");
        })).isInstanceOf(IllegalStateException.class);
        assertNoReview();
        assertThat(objects).containsExactly(temp);
        verify(s3).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void commitTempDeleteFailureDoesNotTurnSuccessfulDbCommitIntoFailure() {
        when(s3.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(503).build());
        assertThatCode(() -> service.createReview(createRequest(), member)).doesNotThrowAnyException();
        assertThat(reviewId()).isPositive();
        assertThat(objects).contains(temp).hasSize(2);
        verify(s3).deleteObject(DeleteObjectRequest.builder().bucket("test-bucket").key(temp).build());
        // Lifecycle can still remove the failed immediate cleanup's source safely.
        objects.remove(temp);
        assertThat(objects).hasSize(1).allMatch(key -> key.startsWith("review/"));
    }

    @Test
    void rollbackPermanentDeleteFailurePreservesOriginalExceptionAndDbRollback() {
        when(s3.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(503).build());
        RuntimeException original = new IllegalStateException("original business failure");
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            service.createReview(createRequest(), member);
            throw original;
        })).isSameAs(original);
        assertNoReview();
        assertThat(objects).contains(temp).hasSize(2);
        verify(s3).deleteObject(argThat((DeleteObjectRequest request) -> request.key().startsWith("review/")));
    }

    @Test
    void commitTimeDbConstraintFailureAlsoCompensatesCopy() {
        var bodyCompleted = new java.util.concurrent.atomic.AtomicBoolean();
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            service.createReview(createRequest(), member);
            // Leave invalid managed state for Hibernate to flush during transaction commit.
            Member managedMember = org.hibernate.Hibernate.unproxy(em.find(Member.class, member.getId()), Member.class);
            ReflectionTestUtils.setField(managedMember, "clientId", null);
            verify(s3, never()).deleteObject(any(DeleteObjectRequest.class));
            bodyCompleted.set(true);
        })).isInstanceOf(RuntimeException.class);
        assertThat(bodyCompleted).isTrue();
        assertNoReview();
        assertThat(objects).containsExactly(temp);
        verify(s3).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void repeatingUpdateWithSameTemporaryKeyDoesNotDuplicateOrRecopyImage() {
        service.createReview(createRequest(), member);
        Long id = reviewId();
        ReviewUpdateRequest request = ReviewUpdateRequest.builder().score(4).content("updated")
                .addImageUrls(List.of(temp, temp)).build();
        service.updateReview(id, request, member.getId());
        service.updateReview(id, request, member.getId());
        tx.executeWithoutResult(status -> assertThat(reviews.getReview(id).getImages()).hasSize(1));
        verify(s3, times(1)).copyObject(any(CopyObjectRequest.class));
        verify(s3, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void foreignPermanentKeyAbortsUpdateAndPreservesOriginalReview() {
        service.createReview(createRequest(), member);
        Long id = reviewId();
        assertThatThrownBy(() -> service.updateReview(id, ReviewUpdateRequest.builder().score(1).content("bad")
                .addImageUrls(List.of("review/foreign.webp")).build(), member.getId()))
                .isInstanceOf(auctionTalk.auction.global.exception.CustomApiException.class);
        tx.executeWithoutResult(status -> {
            assertThat(reviews.getReview(id).getContent()).isEqualTo("review");
            assertThat(reviews.getReview(id).getImages()).hasSize(1);
        });
    }

    @Test
    void reviewWithoutImagesStillRegisters() {
        service.createReview(ReviewCreateRequest.builder().score(5).content("text only").build(), member);
        assertThat(reviewId()).isPositive();
        verify(s3, never()).copyObject(any(CopyObjectRequest.class));
    }

    @Test
    void removingImagesThenRollingBackDoesNotDeleteS3Objects() {
        service.createReview(createRequest(), member);
        Long id = reviewId();
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            service.updateReview(id, ReviewUpdateRequest.builder().score(5).content("remove").build(), member.getId());
            throw new IllegalStateException("rollback");
        })).isInstanceOf(IllegalStateException.class);
        tx.executeWithoutResult(status -> assertThat(reviews.getReview(id).getImages()).hasSize(1));
        verify(s3, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void removingImagesDeletesS3OnlyAfterSuccessfulCommit() {
        service.createReview(createRequest(), member);
        Long id = reviewId();
        when(s3.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(DeleteObjectsResponse.builder().build());
        tx.executeWithoutResult(status -> {
            service.updateReview(id, ReviewUpdateRequest.builder().score(5).content("remove").build(), member.getId());
            verify(s3, never()).deleteObjects(any(DeleteObjectsRequest.class));
        });
        verify(s3).deleteObjects(any(DeleteObjectsRequest.class));
        tx.executeWithoutResult(status -> assertThat(reviews.getReview(id).getImages()).isEmpty());
    }

    private Long reviewId() {
        return tx.execute(status -> em.createQuery("select r.id from Review r where r.member.id = :id", Long.class)
                .setParameter("id", member.getId()).getSingleResult());
    }

    private void assertNoReview() {
        Long count = tx.execute(status -> em.createQuery("select count(r) from Review r where r.member.id = :id", Long.class)
                .setParameter("id", member.getId()).getSingleResult());
        assertThat(count).isZero();
    }
}
