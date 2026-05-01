package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.entity.Role;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.dto.response.PropertyDetailResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyIdResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;
import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.entity.PropertyPayment;
import auctionTalk.auction.domain.property.mapper.PropertyMapper;
import auctionTalk.auction.domain.property.repository.PropertyPaymentRepository;
import auctionTalk.auction.domain.property.repository.PropertyRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @InjectMocks
    private PropertyServiceImpl propertyService;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PropertyPaymentRepository propertyPaymentRepository;

    @Mock
    private PropertyMapper propertyMapper;

    private Member member;
    private Member owner;
    private PrincipalDetails principalDetails;
    private Property property;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .clientId("buyer-client-id")
                .role(Role.USER)
                .build();

        owner = Member.builder()
                .id(2L)
                .clientId("owner-client-id")
                .role(Role.USER)
                .build();

        principalDetails = new PrincipalDetails(member, Map.of());

        property = Property.builder()
                .id(1L)
                .member(member)
                .build();
    }

    @Nested
    @DisplayName("매물 구매")
    class PurchasePropertyTest {

        @Test
        @DisplayName("매물 구매 요청이 들어오면 권한 검증 후 구매 상태를 반영한다")
        void purchaseProperty_success() {
            // given
            Long propertyId = 1L;
            given(propertyRepository.getProperty(propertyId)).willReturn(property);
            given(propertyRepository.save(property)).willReturn(property);

            // when
            PropertyIdResponse result = propertyService.purchaseProperty(member, propertyId);

            // then
            assertThat(result.getId()).isEqualTo(propertyId);
            then(propertyRepository).should().getProperty(propertyId);
            then(propertyRepository).should().save(property);
        }
    }

    @Nested
    @DisplayName("매물 결제 준비")
    class PreparePropertyPaymentTest {

        @Test
        @DisplayName("이미 결제 대기 중인 매물은 다시 결제를 준비할 수 없다")
        void preparePropertyPayment_fail_whenAlreadyReady() {
            // given
            Long propertyId = 1L;
            given(propertyPaymentRepository.existsByMemberAndPropertyIdAndStatus(
                    member, propertyId, PaymentStatus.PENDING
            )).willReturn(true);

            // when & then
            assertThatThrownBy(() -> propertyService.preparePropertyPayment(member, propertyId))
                    .isInstanceOf(CustomApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PROPERTY_ALREADY_PURCHASED);

            then(propertyRepository).shouldHaveNoInteractions();
            then(propertyMapper).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("결제 대기 중이 아니면 매물 결제를 준비할 수 있다")
        void preparePropertyPayment_success() {
            // given
            Long propertyId = 1L;
            PropertyPayment propertyPayment = PropertyPayment.builder()
                    .member(member)
                    .property(property)
                    .status(PaymentStatus.PENDING)
                    .build();

            given(propertyPaymentRepository.existsByMemberAndPropertyIdAndStatus(
                    member, propertyId, PaymentStatus.PENDING
            )).willReturn(false);
            given(propertyRepository.getProperty(propertyId)).willReturn(property);
            given(propertyMapper.toPropertyPayment(member, property)).willReturn(propertyPayment);
            given(propertyPaymentRepository.save(propertyPayment)).willReturn(propertyPayment);

            // when
            PropertyIdResponse result = propertyService.preparePropertyPayment(member, propertyId);

            // then
            assertThat(result.getId()).isEqualTo(propertyId);
            then(propertyRepository).should().getProperty(propertyId);
            then(propertyMapper).should().toPropertyPayment(member, property);
            then(propertyPaymentRepository).should().save(propertyPayment);
        }
    }

    @Nested
    @DisplayName("매물 상세 조회")
    class InquiryPropertyDetailTest {

        @Test
        @DisplayName("결제 성공 이력이 없는 사용자는 매물 상세를 조회할 수 없다")
        void inquiryPropertyDetail_fail_whenPaymentNotFound() {
            // given
            Long propertyId = 1L;
            given(propertyPaymentRepository.existsByMemberAndPropertyIdAndStatus(
                    member, propertyId, PaymentStatus.SUCCESS
            )).willReturn(false);

            // when & then
            assertThatThrownBy(() -> propertyService.inquiryPropertyDetail(member, propertyId))
                    .isInstanceOf(CustomApiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);

            then(propertyRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("결제 성공 이력이 있으면 매물 상세 정보를 조회할 수 있다")
        void inquiryPropertyDetail_success() {
            // given
            Long propertyId = 1L;
            PropertyDetailResponse response = PropertyDetailResponse.builder()
                    .id(propertyId)
                    .build();

            given(propertyPaymentRepository.existsByMemberAndPropertyIdAndStatus(
                    member, propertyId, PaymentStatus.SUCCESS
            )).willReturn(true);
            given(propertyRepository.getProperty(propertyId)).willReturn(property);
            given(propertyMapper.toPropertyDetailResponse(property)).willReturn(response);

            // when
            PropertyDetailResponse result = propertyService.inquiryPropertyDetail(member, propertyId);

            // then
            assertThat(result).isEqualTo(response);
            then(propertyRepository).should().getProperty(propertyId);
            then(propertyMapper).should().toPropertyDetailResponse(property);
        }
    }

    @Nested
    @DisplayName("매물 목록 조회")
    class InquiryPropertiesTest {

        @Test
        @DisplayName("매물 목록 조회 시 각 매물의 결제 여부를 함께 반영해 응답한다")
        void inquiryProperties_success() {
            // given
            Property property1 = Property.builder().id(1L).member(owner).build();
            Property property2 = Property.builder().id(2L).member(owner).build();

            PropertySummaryResponse summary1 = PropertySummaryResponse.builder()
                    .id(1L)
                    .isPurchased(true)
                    .build();

            PropertySummaryResponse summary2 = PropertySummaryResponse.builder()
                    .id(2L)
                    .isPurchased(false)
                    .build();

            Page<Property> propertyPage = new PageImpl<>(List.of(property1, property2));
            Page<PropertySummaryResponse> summaryPage = new PageImpl<>(List.of(summary1, summary2));
            PropertyPagingResponse pagingResponse = PropertyPagingResponse.builder()
                    .properties(List.of(summary1, summary2))
                    .build();

            given(propertyRepository.findAllByMemberIdAndIsPurchased(eq(member.getId()), eq(true), any()))
                    .willReturn(propertyPage);

            given(propertyPaymentRepository.existsByMemberAndPropertyIdAndStatus(
                    member, 1L, PaymentStatus.SUCCESS
            )).willReturn(true);
            given(propertyPaymentRepository.existsByMemberAndPropertyIdAndStatus(
                    member, 2L, PaymentStatus.SUCCESS
            )).willReturn(false);

            given(propertyMapper.toPropertySummaryResponse(property1, true)).willReturn(summary1);
            given(propertyMapper.toPropertySummaryResponse(property2, false)).willReturn(summary2);
            given(propertyMapper.toPropertyPagingResponse(any(Page.class))).willReturn(pagingResponse);

            // when
            PropertyPagingResponse result = propertyService.inquiryProperties(principalDetails, true, 0, 10);

            // then
            assertThat(result).isEqualTo(pagingResponse);
            then(propertyMapper).should().toPropertySummaryResponse(property1, true);
            then(propertyMapper).should().toPropertySummaryResponse(property2, false);
            then(propertyMapper).should().toPropertyPagingResponse(any(Page.class));
        }
    }
}