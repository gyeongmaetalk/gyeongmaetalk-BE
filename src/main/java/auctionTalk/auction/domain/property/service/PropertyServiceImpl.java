package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.property.dto.response.PropertyDetailResponse;
import auctionTalk.auction.domain.property.dto.response.PropertyPagingResponse;
import auctionTalk.auction.domain.property.dto.response.PropertySummaryResponse;
import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.mapper.PropertyMapper;
import auctionTalk.auction.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService{

    private final PropertyRepository propertyRepository;
    private final PropertyImageService propertyImageService;
    private final PropertyMapper propertyMapper;

    @Override
    @Transactional(readOnly = true)
    public PropertyDetailResponse inquiryPropertyDetail(Long propertyId){
        Property property = propertyRepository.getProperty(propertyId);

        return propertyMapper.toPropertyDetailResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyPagingResponse<PropertySummaryResponse> inquiryProperties(PrincipalDetails principal, int page, int size){

        Page<Property> properties = propertyRepository.findAllByMemberId(principal.getMember().getId(), PageRequest.of(page, size));

        return propertyMapper.toPropertyPagingResponse(properties.map(propertyMapper::toPropertySummaryResponse));
    }
}
