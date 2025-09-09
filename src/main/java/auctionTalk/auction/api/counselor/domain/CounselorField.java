package auctionTalk.auction.api.counselor.domain;

import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselorField extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Counselor counselor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Field field;
}
