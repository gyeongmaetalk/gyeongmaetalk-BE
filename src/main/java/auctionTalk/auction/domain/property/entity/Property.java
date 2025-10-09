package auctionTalk.auction.domain.property.entity;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Property extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Counselor counselor;

    private String name;

    private Integer area;

    private Long appraisedPrice;

    private Long minPrice;

    private String address;

    private String caseNumber;

    private String caseTitle;

    private String courtName;

    private LocalDate registrationDate;

    private String status;

    private LocalDate commencementDate;

    private String expertComment;

    private String debtor;

    private String creditor;

    private String owner;

    private String tenant;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<PropertyImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<AuctionSchedule> auctionSchedules = new ArrayList<>();

    public void changeImages(List<PropertyImage> propertyImages) {
        // 새로운 이미지로 변경
        this.images = propertyImages;
    }
}
