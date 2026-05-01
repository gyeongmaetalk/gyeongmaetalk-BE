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
import auctionTalk.auction.domain.property.mapper.PropertyMapper;
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


}