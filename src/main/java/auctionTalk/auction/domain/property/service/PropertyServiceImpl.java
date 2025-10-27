package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.fcm.service.FcmService;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.payment.service.PaymentService;
import auctionTalk.auction.domain.property.dto.request.PropertyCreateRequest;
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
    private final PaymentService paymentService;
    private final FcmService fcmService;

    @Value("${property.fixed-amount}")
    private Long fixedAmount;

    @Value("${property.fixed-name}")
    private String fixedName;

    @Override
    @Transactional
    public PropertyIdResponse createProperty(Member member, Counselor counselor, PropertyCreateRequest request){

        Property newProperty = propertyMapper.toProperty(member, counselor, request);

        propertyRepository.save(newProperty);

        String fcmToken = member.getFcmToken();

        ParamValidator.validateFcmToken(fcmToken);

        String title = "추천 매물";
        String body = "추천 매물을 올려주셨어요.";

        fcmService.sendPushPropertyNotification(fcmToken, title, body, member, newProperty);

        return new PropertyIdResponse(newProperty.getId());
    }

    @Override
    @Transactional
    public PropertyIdResponse purchaseProperty(Member member, Long propertyId){
        ParamValidator.validModify(member.getId(), propertyId); // 권한 유효성 검사
        Property property = propertyRepository.getProperty(propertyId);

        property.purchase();

        propertyRepository.save(property);
        return new PropertyIdResponse(propertyId);
    }

    @Override
    @Transactional
    public PropertyPreparePaymentResponse preparePropertyPayment(Member member, Long propertyId) {
        Long amount = this.fixedAmount;
        String orderName = this.fixedName;

        Property property = propertyRepository.getProperty(propertyId);

        String orderId = generateUniqueOrderId(member.getId());

        PropertyPayment payment = propertyMapper.toPropertyPayment(member, property, orderId, amount, orderName);

        propertyPaymentRepository.save(payment);

        return propertyMapper.toPropertyPreparePaymentResponse(payment);
    }

    private String generateUniqueOrderId(Long memberId) {
        return "SUB-" + memberId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    @Transactional
    public PaymentResultResponse confirmPropertyPayment(Long propertyId, PaymentConfirmRequest request){

        PropertyPayment payment = propertyPaymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND));


        PaymentResultResponse response = paymentService.callTossPaymentApi(request);

        payment.updatePaymentKey(request.getPaymentKey());
        payment.updatePaymentStatus(PaymentStatus.SUCCESS);

        return response;
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
    public PropertyPagingResponse<PropertySummaryResponse> inquiryProperties(PrincipalDetails principal, int page, int size){

        Page<Property> properties = propertyRepository.findAllByMemberId(principal.getMember().getId(), PageRequest.of(page, size));

        Member member = principal.getMember();

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
