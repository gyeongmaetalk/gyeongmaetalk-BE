package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.dto.request.PropertyCreateRequest;
import auctionTalk.auction.domain.property.dto.request.PropertyUpdateRequest;
import auctionTalk.auction.domain.property.dto.response.PropertyDetailResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyIdResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;

public interface AdminPropertyService {

    PropertyIdResponse createProperty(PropertyCreateRequest request);
    PropertyDetailResponse getPropertyDetail(Long propertyId);
    PropertyIdResponse updateProperty(Long propertyId, PropertyUpdateRequest request);
    PropertyIdResponse deleteProperty(Long propertyId);
    PropertyPagingResponse<PropertySummaryResponse> inquiryPropertiesByMember(Long memberId, int page, int size);
    PaymentResultResponse updatePropertyPaymentStatus(Long propertyId, PaymentStatus status);

}
