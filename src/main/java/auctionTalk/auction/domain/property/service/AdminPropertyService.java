package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.property.dto.request.PropertyCreateRequest;
import auctionTalk.auction.domain.property.dto.request.PropertyUpdateRequest;
import auctionTalk.auction.domain.property.dto.response.PropertyDetailResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyIdResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;

public interface AdminPropertyService {

    PropertyIdResponse createProperty(Long memberId, PropertyCreateRequest request);
    PropertyDetailResponse getPropertyDetail(Long propertyId);
    PropertyIdResponse updateProperty(Long propertyId, PropertyUpdateRequest request);
    PropertyIdResponse deleteProperty(Long propertyId);
    PropertyPagingResponse<PropertySummaryResponse> inquiryPropertiesByMember(Long memberId, int page, int size);
}
