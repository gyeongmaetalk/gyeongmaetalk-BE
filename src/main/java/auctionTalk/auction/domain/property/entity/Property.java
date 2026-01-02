package auctionTalk.auction.domain.property.entity;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.property.dto.request.PropertyUpdateRequest;
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

    private String buildingType;

    private boolean isPurchased = false;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<PropertyImage> images = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private List<AuctionSchedule> auctionSchedules = new ArrayList<>();

    public void updateProperty(PropertyUpdateRequest request){
        this.name = request.getName();
        this.area = request.getArea();
        this.appraisedPrice = request.getAppraisedPrice();
        this.minPrice = request.getMinPrice();
        this.address = request.getAddress();
        this.caseNumber = request.getCaseNumber();
        this.caseTitle = request.getCaseTitle();
        this.courtName = request.getCourtName();
        this.registrationDate = request.getRegistrationDate();
        this.status = request.getStatus();
        this.commencementDate = request.getCommencementDate();
        this.expertComment = request.getExpertComment();
        this.debtor = request.getDebtor();
        this.creditor = request.getCreditor();
        this.owner = request.getOwner();
        this.tenant = request.getTenant();
        this.buildingType = request.getBuildingType();
    }

    public void changeImages(List<PropertyImage> propertyImages) {
        // 새로운 이미지로 변경
        this.images = propertyImages;
    }

    public String getThumbnail() {
        return this.images.stream()
                .findFirst()
                .map(PropertyImage::getUrl)
                .orElse(null);
    }

    public void purchase(){
        this.isPurchased = true;
    }

    public List<String> updateImages(List<String> remainKeys, List<String> addKeys) {

        // 🔥 삭제될 key 목록 수집
        List<String> deleteKeys = this.images.stream()
                .map(PropertyImage::getUrl)
                .filter(key -> !remainKeys.contains(key))
                .toList();

        this.images.removeIf(img -> !remainKeys.contains(img.getUrl()));

        // 2) 추가
        if (addKeys != null) {
            for (String key : addKeys) {
                PropertyImage newImg = PropertyImage.builder()
                        .url(key)
                        .property(this)
                        .build();
                this.images.add(newImg);
            }
        }

        // 🔥 삭제해야 하는 key들을 서비스로 전달
        return deleteKeys;
    }
}
