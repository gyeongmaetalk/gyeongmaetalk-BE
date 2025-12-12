package auctionTalk.auction.domain.qna.entity;

import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Qna extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 250)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QnaCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QnaStatus status;

    @OneToOne(mappedBy = "qna", cascade = CascadeType.ALL)
    private QnaAnswer answer;

    public void markAnswered(QnaAnswer answer) {
        this.answer = answer;
        this.status = QnaStatus.ANSWERED;
    }
}
