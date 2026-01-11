package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.domain.counsel.entity.CounselStatus;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.fcm.service.FcmService;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.NotificationSetting;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.dto.request.PropertyCreateRequest;
import auctionTalk.auction.domain.property.dto.request.PropertyUpdateRequest;
import auctionTalk.auction.domain.property.dto.response.PropertyDetailResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyIdResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;
import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.entity.PropertyImage;
import auctionTalk.auction.domain.property.entity.PropertyPayment;
import auctionTalk.auction.domain.property.mapper.PropertyMapper;
import auctionTalk.auction.domain.property.repository.PropertyImageRepository;
import auctionTalk.auction.domain.property.repository.PropertyPaymentRepository;
import auctionTalk.auction.domain.property.repository.PropertyRepository;
import auctionTalk.auction.domain.subscription.entity.Subscription;
import auctionTalk.auction.domain.subscription.repository.SubscriptionRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.global.validation.ParamValidator;
import auctionTalk.auction.utils.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminPropertyServiceImpl implements AdminPropertyService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyPaymentRepository propertyPaymentRepository;
    private final PropertyMapper propertyMapper;
    private final FcmService fcmService;
    private final PropertyImageRepository propertyImageRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public PropertyIdResponse createProperty(PropertyCreateRequest request){

        Member member = memberRepository.getMember(request.getMemberId());

        Subscription subscription = subscriptionRepository.getSubscriptionByMemberId(request.getMemberId());

        Counselor counselor = subscription.getCounselor();

        Property newProperty = propertyMapper.toProperty(member, counselor, request);
        propertyRepository.save(newProperty);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<PropertyImage> images = request.getImageUrls().stream()
                    .map(url -> propertyMapper.toPropertyImage(newProperty, url))
                    .toList();

            propertyImageRepository.saveAll(images);
        }

        // 추천 매물 알림 설정 확인
        NotificationSetting setting = member.getNotificationSetting();
        if (!setting.isPropertyNotificationEnabled()) {
            return new PropertyIdResponse(newProperty.getId());
        }

        String fcmToken = member.getFcmToken();

        ParamValidator.validateFcmToken(fcmToken);

        String title = "추천 매물";
        String body = "추천 매물을 올려주셨어요.";

        fcmService.sendPushPropertyNotification(fcmToken, title, body, member, newProperty);

        return new PropertyIdResponse(newProperty.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyPagingResponse<PropertySummaryResponse> inquiryPropertiesByMember(Long memberId, int page, int size){

        Member member = memberRepository.getMember(memberId);
        Page<Property> properties = propertyRepository.findAllByMemberId(memberId, PageRequest.of(page, size));

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

    @Override
    @Transactional(readOnly = true)
    public PropertyDetailResponse getPropertyDetail(Long propertyId){
        Property property = propertyRepository.getProperty(propertyId);

        return propertyMapper.toPropertyDetailResponse(property);
    }

    @Override
    @Transactional
    public PropertyIdResponse updateProperty(Long propertyId, PropertyUpdateRequest request){
        Property property = propertyRepository.getProperty(propertyId);

        property.updateProperty(request);

        List<String> remainKeys = Optional.ofNullable(request.getRemainImageUrls())
                .orElseGet(ArrayList::new);

        List<String> addKeys = Optional.ofNullable(request.getAddImageUrls())
                .orElseGet(ArrayList::new);

        List<String> deleteKeys = property.updateImages(remainKeys, addKeys);

        if (!deleteKeys.isEmpty()) {
            s3Service.deleteFiles(deleteKeys);
        }

        return new PropertyIdResponse(propertyId);
    }

    @Override
    @Transactional
    public PropertyIdResponse deleteProperty(Long propertyId){
        propertyRepository.deleteById(propertyId);

        return new PropertyIdResponse(propertyId);
    }

    @Override
    @Transactional
    public PaymentResultResponse updatePropertyPaymentStatus(Long propertyId, PaymentStatus status){

        Property property = propertyRepository.getProperty(propertyId);

        PropertyPayment payment = propertyPaymentRepository.findByProperty(property)
                .orElseThrow(() -> new CustomApiException(ErrorCode.PAYMENT_NOT_FOUND));

        if(status == PaymentStatus.SUCCESS) {
            payment.updatePaymentStatus(status);
        }
        if(status == PaymentStatus.FAIL) {
            payment.updatePaymentStatus(status);
        }

        return new PaymentResultResponse(payment.getStatus());
    }

}
