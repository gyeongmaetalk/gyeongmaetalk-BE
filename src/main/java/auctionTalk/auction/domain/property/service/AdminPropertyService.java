package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;

public interface AdminPropertyService {

    PropertyPagingResponse<PropertySummaryResponse> inquiryPropertiesByMember(Long memberId, int page, int size);
}
