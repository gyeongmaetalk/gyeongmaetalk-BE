package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.dto.request.PropertyCreateRequest;
import auctionTalk.auction.domain.property.dto.response.*;

public interface PropertyService {

    PropertyIdResponse purchaseProperty(Member member, Long propertyId);
    PropertyIdResponse preparePropertyPayment(Member member, Long propertyId);

    PropertyDetailResponse inquiryPropertyDetail(Member member, Long propertyId);
    PropertyPagingResponse<PropertySummaryResponse> inquiryProperties(PrincipalDetails principal, Boolean isPurchased, int page, int size);
}
