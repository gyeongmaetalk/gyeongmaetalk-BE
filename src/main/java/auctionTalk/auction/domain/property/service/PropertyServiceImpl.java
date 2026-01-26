package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.dto.response.*;
import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.entity.PropertyPayment;
import auctionTalk.auction.domain.property.mapper.PropertyMapper;
import auctionTalk.auction.domain.property.repository.PropertyPaymentRepository;
import auctionTalk.auction.domain.property.repository.PropertyRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.global.validation.ParamValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService{

    private final PropertyRepository propertyRepository;
    private final PropertyPaymentRepository propertyPaymentRepository;
    private final PropertyMapper propertyMapper;

    @Value("${property.fixed-amount}")
    private Long fixedAmount;

    @Value("${property.fixed-name}")
    private String fixedName;

    @Override
    @Transactional
    public PropertyIdResponse purchaseProperty(Member member, Long propertyId){
        Property property = propertyRepository.getProperty(propertyId);

        ParamValidator.validModify(member.getId(), property.getMember().getId()); // 권한 유효성 검사

        property.purchase();

        propertyRepository.save(property);
        return new PropertyIdResponse(propertyId);
    }

    @Override
    @Transactional
    public PropertyIdResponse preparePropertyPayment(Member member, Long propertyId) {

        // 중복 구매 신청 검사 추가.
        if (propertyPaymentRepository.existsByMemberAndPropertyIdAndStatus(
                member, propertyId, PaymentStatus.READY)) {
            throw new CustomApiException(ErrorCode.PROPERTY_ALREADY_PURCHASED);
        }

        Property property = propertyRepository.getProperty(propertyId);

        PropertyPayment payment = propertyMapper.toPropertyPayment(member, property);

        propertyPaymentRepository.save(payment);

        return new PropertyIdResponse(propertyId);
    }

    private String generateUniqueOrderId(Long memberId) {
        return "SUB-" + memberId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }


    @Override
    @Transactional(readOnly = true)
    public PropertyDetailResponse inquiryPropertyDetail(Member member, Long propertyId){

        boolean hasPaid = propertyPaymentRepository.existsByMemberAndPropertyIdAndStatus(
                member, propertyId, PaymentStatus.SUCCESS
        );

        if (!hasPaid) {
            throw new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        Property property = propertyRepository.getProperty(propertyId);

        return propertyMapper.toPropertyDetailResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyPagingResponse<PropertySummaryResponse> inquiryProperties(PrincipalDetails principal, Boolean isPurchased, int page, int size){

        Member member = principal.getMember();
        Page<Property> properties = propertyRepository.findAllByMemberIdAndIsPurchased(member.getId(), isPurchased, PageRequest.of(page, size));

        Page<PropertySummaryResponse> responsePage = properties.map(property -> {
            boolean hasPaid = propertyPaymentRepository.existsByMemberAndPropertyIdAndStatus(
                    member,
                    property.getId(),
                    PaymentStatus.SUCCESS
            );
            return propertyMapper.toPropertySummaryResponse(property, hasPaid);
        });

        return propertyMapper.toPropertyPagingResponse(responsePage);
    }
}
