package auctionTalk.auction.domain.counsel.entity;

import auctionTalk.auction.domain.counsel.dto.request.CounselFormUpdateRequest;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounselForm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Member member;

    private String purpose;

    private String area;

    private String serviceType;

    private String interest;

    private String participantType;

    public void updateCounselForm(CounselFormUpdateRequest request, Member member){
        this.member = member;
        this.purpose = request.getPurpose();
        this.area = request.getArea();
        this.serviceType = request.getServiceType();
        this.interest = request.getInterest();
        this.participantType = request.getParticipantType();
    }
}
