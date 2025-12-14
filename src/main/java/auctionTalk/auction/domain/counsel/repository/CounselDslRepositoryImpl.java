package auctionTalk.auction.domain.counsel.repository;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counsel.entity.QCounsel;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CounselDslRepositoryImpl implements CounselDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Counsel> searchByConditions(
            List<CounselStatus> statuses,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        QCounsel counsel = QCounsel.counsel;

        BooleanBuilder builder = new BooleanBuilder();

        // 상태 필터
        if (statuses != null && !statuses.isEmpty()) {
            builder.and(counsel.counselStatus.in(statuses));
        }

        // 날짜 필터 (상담 날짜 기준)
        if (startDate != null) {
            builder.and(counsel.counselDate.goe(startDate));
        }
        if (endDate != null) {
            builder.and(counsel.counselDate.loe(endDate));
        }

        List<Counsel> contents = queryFactory
                .selectFrom(counsel)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(counsel.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(counsel.count())
                .from(counsel)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(contents, pageable, total);
    }
}
