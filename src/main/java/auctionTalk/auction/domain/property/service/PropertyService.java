package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.property.dto.response.*;

public interface PropertyService {
    PropertyIdResponse purchaseProperty(Member member, Long propertyId);
    PropertyPreparePaymentResponse preparePropertyPayment(Member member, Long propertyId);
    PaymentResultResponse confirmPropertyPayment(Long propertyId, PaymentConfirmRequest paymentConfirmRequest);
    PropertyDetailResponse inquiryPropertyDetail(Member member, Long propertyId);
    PropertyPagingResponse<PropertySummaryResponse> inquiryProperties(PrincipalDetails principal, int page, int size);
}
