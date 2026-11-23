package auctionTalk.auction.domain.review.service;

import auctionTalk.auction.config.QuerydslConfig;
import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.domain.review.dto.response.ReviewCursorResponse;
import auctionTalk.auction.domain.review.dto.response.ReviewSummaryResponse;
import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.review.entity.ReviewImage;
import auctionTalk.auction.domain.review.mapper.ReviewMapper;
import auctionTalk.auction.domain.review.repository.ReviewRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.utils.s3.S3Service;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * 커서 조회를 실제 JPA·DB(H2)로 검증한다.
 * - 같은 작성 시각이 섞여 있어도 끝까지 넘겼을 때 누락·중복이 없어야 한다.
 * - 한 페이지를 만드는 데 리뷰 수와 상관없이 쿼리가 2번(리뷰+회원+상담 1번, 이미지 1번)이어야 한다.
 */
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import(QuerydslConfig.class)
class ReviewCursorPagingTest {

    private static final int REVIEW_COUNT = 25;
    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 16, 10, 0, 0, 500_000_000);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EntityManager em;

    private ReviewServiceImpl reviewService;
    private Statistics statistics;
    private List<Long> expectedOrder;

    @BeforeEach
    void setUp() {
        S3Service s3Service = mock(S3Service.class);
        given(s3Service.generatePresignedGetUrl(anyString())).willAnswer(inv -> "https://s3/" + inv.getArgument(0));
        ReviewMapper mapper = new ReviewMapper(s3Service);
        reviewService = new ReviewServiceImpl(null, null, mapper, reviewRepository, null, null, s3Service);

        Counselor counselor = Counselor.builder().name("상담사").experience(5).build();
        em.persist(counselor);

        record Row(long id, LocalDateTime createdAt) {}
        List<Row> rows = new ArrayList<>();

        for (int i = 0; i < REVIEW_COUNT; i++) {
            Member member = Member.builder()
                    .clientId("client-" + i)
                    .loginType(LoginType.values()[0])
                    .role(Role.values()[0])
                    .name("회원" + i)
                    .build();
            em.persist(member);

            Counsel counsel = Counsel.builder()
                    .member(member)
                    .counselor(counselor)
                    .counselDate(LocalDate.of(2026, 11, 1))
                    .counselTime(LocalTime.of(9, 0).plusMinutes(i))
                    .build();
            em.persist(counsel);

            Review review = Review.builder()
                    .member(member)
                    .counsel(counsel)
                    .score(5)
                    .content("리뷰" + i)
                    .images(new ArrayList<>())
                    .build();
            em.persist(review);
            // 절반 정도는 이미지가 있어 이미지 조회가 실제로 일어나게 한다
            if (i % 2 == 0) {
                review.addImage(ReviewImage.builder().url("review/" + i + "/a.jpg").build());
                review.addImage(ReviewImage.builder().url("review/" + i + "/b.jpg").build());
                review.getImages().forEach(em::persist);
            }
            em.flush();

            // 3건씩 같은 작성 시각(마이크로초 포함)을 갖게 해서, 시각만으로는 순서가 정해지지 않는 경우를 만든다
            LocalDateTime createdAt = BASE.minusMinutes(i / 3).plusNanos(123_000);
            em.createNativeQuery("update review set created_at = ?1 where id = ?2")
                    .setParameter(1, createdAt)
                    .setParameter(2, review.getId())
                    .executeUpdate();
            rows.add(new Row(review.getId(), createdAt));
        }
        em.flush();
        em.clear();

        expectedOrder = rows.stream()
                .sorted(Comparator.comparing(Row::createdAt).reversed()
                        .thenComparing(Comparator.comparingLong(Row::id).reversed()))
                .map(Row::id)
                .toList();

        statistics = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    }

    @Test
    @DisplayName("커서를 따라 끝까지 넘기면 모든 리뷰를 최신순으로 누락·중복 없이 한 번씩 받는다")
    void traversesAllReviewsWithoutGapOrDuplicate() {
        List<Long> visited = new ArrayList<>();
        String cursor = null;
        int pages = 0;

        do {
            ReviewCursorResponse<ReviewSummaryResponse> response = reviewService.inquiryReviewsByCursor(null, cursor, 10);
            response.getReviews().forEach(r -> visited.add(r.getReviewId()));
            cursor = response.getNextCursor();
            assertThat(response.isHasNext()).isEqualTo(cursor != null);
            em.clear();
            pages++;
        } while (cursor != null);

        assertThat(pages).isEqualTo(3);
        assertThat(visited).containsExactlyElementsOf(expectedOrder);
    }

    @Test
    @DisplayName("한 페이지를 만드는 쿼리는 리뷰 수와 관계없이 2번이다 (N+1 없음)")
    void pageUsesConstantNumberOfQueries() {
        statistics.clear();

        ReviewCursorResponse<ReviewSummaryResponse> response = reviewService.inquiryReviewsByCursor(null, null, 20);

        assertThat(response.getReviews()).hasSize(20);
        assertThat(response.getReviews()).allSatisfy(r -> assertThat(r.getName()).startsWith("회원"));
        assertThat(response.getReviews().stream().mapToInt(ReviewSummaryResponse::getImageCount).sum()).isPositive();
        // 리뷰+회원+상담 조회 1번 + 이미지 IN 조회 1번
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("마지막 페이지는 hasNext=false, nextCursor=null 이다")
    void lastPage() {
        ReviewCursorResponse<ReviewSummaryResponse> response = reviewService.inquiryReviewsByCursor(null, null, REVIEW_COUNT);

        assertThat(response.getReviews()).hasSize(REVIEW_COUNT);
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("조회 개수가 범위를 벗어나면 400 예외")
    void invalidSize() {
        assertThatThrownBy(() -> reviewService.inquiryReviewsByCursor(null, null, 0))
                .isInstanceOf(CustomApiException.class);
        assertThatThrownBy(() -> reviewService.inquiryReviewsByCursor(null, null, 51))
                .isInstanceOf(CustomApiException.class);
    }
}
