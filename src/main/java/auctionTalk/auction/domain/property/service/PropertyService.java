package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.property.dto.response.PropertyDetailResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;

public interface PropertyService {
    PropertyDetailResponse inquiryPropertyDetail(Long propertyId);
    PropertyPagingResponse<PropertySummaryResponse> inquiryProperties(PrincipalDetails principal, int page, int size);
}
