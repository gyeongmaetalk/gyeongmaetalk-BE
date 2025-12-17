package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;
import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.mapper.PropertyMapper;
import auctionTalk.auction.domain.property.repository.PropertyPaymentRepository;
import auctionTalk.auction.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPropertyServiceImpl implements AdminPropertyService {

    private final MemberRepository memberRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyPaymentRepository propertyPaymentRepository;
    private final PropertyMapper propertyMapper;

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
}
