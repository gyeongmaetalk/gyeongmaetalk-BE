package auctionTalk.auction.repository;

import auctionTalk.auction.config.QuerydslConfig;
import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.fcm.entity.Notification;
import auctionTalk.auction.domain.fcm.entity.NotificationType;
import auctionTalk.auction.domain.fcm.repository.NotificationRepository;
import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.domain.review.dto.response.ReviewStats;
import auctionTalk.auction.domain.review.entity.Review;
import auctionTalk.auction.domain.review.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 데이터가 계속 쌓이는 테이블(리뷰·상담·알림)을 다루는 쿼리를 실제 JPA·DB(H2)로 검증한다.
 */
@DataJpaTest
@Import(QuerydslConfig.class)
class GrowingTableQueryTest {

    @Autowired private EntityManager em;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private CounselRepository counselRepository;
    @Autowired private NotificationRepository notificationRepository;

    private int seq = 0;

    private Member member() {
        Member member = Member.builder()
                .clientId("client-" + (++seq))
                .loginType(LoginType.values()[0])
                .role(Role.values()[0])
                .name("회원" + seq)
                .build();
        em.persist(member);
        return member;
    }

    private Counselor counselor(String name) {
        Counselor counselor = Counselor.builder().name(name).experience(5).build();
        em.persist(counselor);
        return counselor;
    }

    private Counsel counsel(Member member, Counselor counselor, LocalDate date, LocalTime time, boolean pushSent) {
        Counsel counsel = Counsel.builder()
                .member(member)
                .counselor(counselor)
                .counselDate(date)
                .counselTime(time)
                .pushSent(pushSent)
                .build();
        em.persist(counsel);
        return counsel;
    }

    private void review(Counselor counselor, int score) {
        Member member = member();
        Counsel counsel = counsel(member, counselor, LocalDate.of(2026, 11, 1), LocalTime.of(9, 0).plusMinutes(seq), true);
        em.persist(Review.builder()
                .member(member)
                .counsel(counsel)
                .score(score)
                .content("리뷰")
                .images(new ArrayList<>())
                .build());
    }

    @Test
    @DisplayName("상담사 리뷰 개수와 평균 별점을 DB에서 계산하고, 다른 상담사 리뷰는 섞지 않는다")
    void reviewStatsByCounselor() {
        Counselor target = counselor("대상");
        Counselor other = counselor("다른 상담사");
        review(target, 4);
        review(target, 5);
        review(target, 3);
        review(other, 1);
        em.flush();
        em.clear();

        ReviewStats stats = reviewRepository.getStatsByCounselor(target);

        assertThat(stats.count()).isEqualTo(3);
        assertThat(stats.average()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("리뷰가 없는 상담사는 개수 0, 평균 0이다")
    void reviewStatsWithoutReviews() {
        Counselor counselor = counselor("신규");
        em.flush();

        ReviewStats stats = reviewRepository.getStatsByCounselor(counselor);

        assertThat(stats.count()).isZero();
        assertThat(stats.average()).isZero();
    }

    @Test
    @DisplayName("리뷰 알림 대상은 기준 시각 이전이고 아직 보내지 않은 상담만이다(날짜·시간 경계 포함)")
    void pushTargetsRespectDateTimeBoundary() {
        Counselor counselor = counselor("상담사");
        LocalDateTime threshold = LocalDateTime.of(2026, 11, 26, 10, 30);

        Counsel previousDay = counsel(member(), counselor, threshold.toLocalDate().minusDays(1), LocalTime.of(23, 0), false);
        Counsel sameDayBefore = counsel(member(), counselor, threshold.toLocalDate(), LocalTime.of(10, 0), false);
        Counsel exactlyThreshold = counsel(member(), counselor, threshold.toLocalDate(), LocalTime.of(10, 30), false);
        counsel(member(), counselor, threshold.toLocalDate(), LocalTime.of(11, 0), false);      // 아직 기준 시각 전이 아님
        counsel(member(), counselor, threshold.toLocalDate().plusDays(1), LocalTime.of(9, 0), false); // 내일
        counsel(member(), counselor, threshold.toLocalDate().minusDays(3), LocalTime.of(9, 0), true); // 이미 처리함
        em.flush();
        em.clear();

        List<Counsel> targets = counselRepository.findAllForPush(threshold);

        assertThat(targets).extracting(Counsel::getId)
                .containsExactlyInAnyOrder(previousDay.getId(), sameDayBefore.getId(), exactlyThreshold.getId());
    }

    @Test
    @DisplayName("보관 기간이 지난 알림만 지운다")
    void deleteExpiredNotifications() {
        Member member = member();
        LocalDateTime now = LocalDateTime.of(2026, 11, 26, 4, 0);
        Notification old = notification(member, now.minusDays(Notification.RETENTION_DAYS + 1));
        Notification recent = notification(member, now.minusDays(Notification.RETENTION_DAYS - 1));
        em.clear();

        int deleted = notificationRepository.deleteAllCreatedBefore(now.minusDays(Notification.RETENTION_DAYS));

        assertThat(deleted).isEqualTo(1);
        assertThat(notificationRepository.findById(old.getId())).isEmpty();
        assertThat(notificationRepository.findById(recent.getId())).isPresent();
    }

    private Notification notification(Member member, LocalDateTime createdAt) {
        Notification notification = Notification.builder()
                .member(member)
                .title("제목")
                .body("내용")
                .isRead(false)
                .type(NotificationType.COUNSEL_FINISHED)
                .build();
        em.persist(notification);
        em.flush();
        em.createNativeQuery("update notification set created_at = ?1 where id = ?2")
                .setParameter(1, createdAt)
                .setParameter(2, notification.getId())
                .executeUpdate();
        return notification;
    }
}
