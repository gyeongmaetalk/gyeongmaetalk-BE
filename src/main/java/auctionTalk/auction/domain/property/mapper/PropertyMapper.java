package auctionTalk.auction.domain.property.mapper;

import auctionTalk.auction.domain.property.dto.response.PropertyDetailResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;
import auctionTalk.auction.domain.property.entity.AuctionSchedule;
import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.entity.PropertyImage;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PropertyMapper {

    public PropertyImage toPropertyImage(Property property, String url) {
        return PropertyImage.builder()
                .property(property)
                .url(url)
                .build();
    }

    public PropertySummaryResponse toPropertySummaryResponse(Property property) {
        return PropertySummaryResponse.builder()
                .id(property.getId())
                .address(property.getAddress())
                .area(property.getArea())
                .biddingDate(property.getAuctionSchedules().stream()
                        .findFirst()
                        .map(AuctionSchedule::getDate)
                        .orElse(null))
                .appraisedPrice(property.getAppraisedPrice())
                .minPrice(property.getMinPrice())
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
