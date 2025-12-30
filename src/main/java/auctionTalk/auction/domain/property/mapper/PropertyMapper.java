package auctionTalk.auction.domain.property.mapper;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.dto.request.PropertyCreateRequest;
import auctionTalk.auction.domain.property.dto.response.*;
import auctionTalk.auction.domain.property.entity.AuctionSchedule;
import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.entity.PropertyImage;
import auctionTalk.auction.domain.property.entity.PropertyPayment;
import auctionTalk.auction.domain.subscription.dto.response.SubscriptionPreparePaymentResponse;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PropertyMapper {

    public Property toProperty(Member member, Counselor counselor, PropertyCreateRequest request) {
        return Property.builder()
                .member(member)
                .counselor(counselor)
                .name(request.getName())
                .area(request.getArea())
                .appraisedPrice(request.getAppraisedPrice())
                .minPrice(request.getMinPrice())
                .address(request.getAddress())
                .caseNumber(request.getCaseNumber())
                .caseTitle(request.getCaseTitle())
                .courtName(request.getCourtName())
                .registrationDate(request.getRegistrationDate())
                .status(request.getStatus())
                .commencementDate(request.getCommencementDate())
                .expertComment(request.getExpertComment())
                .debtor(request.getDebtor())
                .creditor(request.getCreditor())
                .owner(request.getOwner())
                .tenant(request.getTenant())
                .buildingType(request.getBuildingType())
                .build();
    }

    public PropertyImage toPropertyImage(Property property, String url) {
        return PropertyImage.builder()
                .property(property)
                .url(url)
                .build();
    }

    public PropertySummaryResponse toPropertySummaryResponse(Property property, boolean payment) {
        return PropertySummaryResponse.builder()
                .id(property.getId())
                .name(property.getName())
                .address(property.getAddress())
                .area(property.getArea())
                .biddingDate(property.getAuctionSchedules().stream()
                        .findFirst()
                        .map(AuctionSchedule::getDate)
                        .orElse(null))
                .appraisedPrice(property.getAppraisedPrice())
                .minPrice(property.getMinPrice())
                .isPurchased(property.isPurchased())
                .isPayment(payment)
                .buildingType(property.getBuildingType())
                .updateDate(property.getCreatedAt())
                .images(toImageUrls(property.getImages()))
                .build();
    }

    public AdminPropertySummaryResponse toAdminPropertySummaryResponse(Property property, boolean payment) {
        return AdminPropertySummaryResponse.builder()
                .id(property.getId())
                .memberId(property.getMember().getId())
                .name(property.getName())
                .address(property.getAddress())
                .area(property.getArea())
                .biddingDate(property.getAuctionSchedules().stream()
                        .findFirst()
                        .map(AuctionSchedule::getDate)
                        .orElse(null))
                .appraisedPrice(property.getAppraisedPrice())
                .minPrice(property.getMinPrice())
                .isPurchased(property.isPurchased())
                .isPayment(payment)
                .buildingType(property.getBuildingType())
                .updateDate(property.getCreatedAt())
                .images(toImageUrls(property.getImages()))
                .build();
    }

    public <T> PropertyPagingResponse<T> toPropertyPagingResponse(Page<T> properties) {
        return PropertyPagingResponse.<T>builder()
                .properties(properties.getContent())
                .page(properties.getNumber())
                .totalPages(properties.getTotalPages())
                .totalElements((int) properties.getTotalElements())
                .isFirst(properties.isFirst())
                .isLast(properties.isLast())
                .build();
    }

    public PropertyDetailResponse toPropertyDetailResponse(Property property) {
        return PropertyDetailResponse.builder()
                .name(property.getName())
                .area(property.getArea())
                .appraisedPrice(property.getAppraisedPrice())
                .minPrice(property.getMinPrice())
                .address(property.getAddress())
                .caseNumber(property.getCaseNumber())
                .caseTitle(property.getCaseTitle())
                .courtName(property.getCourtName())
                .registrationDate(property.getRegistrationDate())
                .commencementDate(property.getCommencementDate())
                .status(property.getStatus())
                .debtor(property.getDebtor())
                .creditor(property.getCreditor())
                .owner(property.getOwner())
                .tenant(property.getTenant())
                .scheduleInfos(toScheduleInfo(property.getAuctionSchedules()))
                .isPurchased(property.isPurchased())
                .expertComment(property.getExpertComment())
                .buildingType(property.getBuildingType())
                .updateDate(property.getCreatedAt())
                .images(toImageUrls(property.getImages()))
                .build();
    }


    public PropertyPayment toPropertyPayment(Member member, Property property, String orderId, Long amount, String orderName){
        return PropertyPayment.builder()
                .member(member)
                .property(property)
                .orderId(orderId)
                .orderName(orderName)
                .amount(amount)
                .status(PaymentStatus.READY)
                .build();
    }

    public PropertyPreparePaymentResponse toPropertyPreparePaymentResponse(PropertyPayment payment){
        return PropertyPreparePaymentResponse.builder()
                .propertyId(payment.getProperty().getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .orderName(payment.getOrderName())
                .build();
    }

    private List<PropertyDetailResponse.ScheduleInfo> toScheduleInfo(List<AuctionSchedule> schedules) {
        return schedules.stream()
                .map(schedule -> PropertyDetailResponse.ScheduleInfo.builder()
                        .round(schedule.getRound())
                        .date(schedule.getDate())
                        .price(schedule.getPrice())
                        .result(schedule.getResult())
                        .build())
                .toList();
    }

    private List<String> toImageUrls(List<PropertyImage> images) {
        return images.stream().map(PropertyImage::getUrl).toList();
    }
}
