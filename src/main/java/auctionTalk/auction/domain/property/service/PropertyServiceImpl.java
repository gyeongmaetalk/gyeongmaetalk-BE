package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.payment.entity.PaymentStatus;
import auctionTalk.auction.domain.property.dto.response.*;
import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.mapper.PropertyMapper;
import auctionTalk.auction.domain.property.repository.PropertyRepository;
import auctionTalk.auction.domain.viewticket.entity.MemberViewTicketWallet;
import auctionTalk.auction.domain.viewticket.repository.MemberViewTicketWalletRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.global.validation.ParamValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService{

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final MemberViewTicketWalletRepository memberViewTicketWalletRepository;

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
    public PropertyDetailResponse inquiryPropertyDetail(Member member, Long propertyId){
        Property property = propertyRepository.getProperty(propertyId);

        // 1차 확인: 이미 열람 가능하면 바로 반환
        if (property.isPayment()) {
            return propertyMapper.toPropertyDetailResponse(property);
        }

        // 같은 회원의 동시 차감을 막기 위해 지갑 lock
        MemberViewTicketWallet wallet = memberViewTicketWalletRepository.findByMemberIdForUpdate(member.getId())
                .orElseThrow(() -> new CustomApiException(ErrorCode.VIEW_TICKET_WALLET_NOT_FOUND));

        // 락 잡은 뒤 다시 조회
        Property lockedProperty = propertyRepository.getPropertyByIdAndMemberId(propertyId, member.getId());

        // 2차 확인: 기다리는 동안 다른 요청이 이미 열람 처리했을 수 있음
        if (lockedProperty.isPayment()) {
            return propertyMapper.toPropertyDetailResponse(lockedProperty);
        }

        if (wallet.getBalance() <= 0) {
            throw new CustomApiException(ErrorCode.VIEW_TICKET_NOT_ENOUGH);
        }

        wallet.decrease(1);
        lockedProperty.markPaymentCompleted();

        return propertyMapper.toPropertyDetailResponse(lockedProperty);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyPagingResponse<PropertySummaryResponse> inquiryProperties(PrincipalDetails principal, int page, int size) {
        Member member = principal.getMember();

        Page<Property> properties = propertyRepository.findAllByMemberId(
                member.getId(),
                PageRequest.of(page, size)
        );

        Page<PropertySummaryResponse> responsePage = properties.map(property ->
                propertyMapper.toPropertySummaryResponse(property, property.isPayment())
        );

        return propertyMapper.toPropertyPagingResponse(responsePage);
    }
}
