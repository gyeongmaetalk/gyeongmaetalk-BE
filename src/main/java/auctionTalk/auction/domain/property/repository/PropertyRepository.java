package auctionTalk.auction.domain.property.repository;

import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    default Property getProperty(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.PROPERTY_NOT_FOUND));
    }

    Page<Property> findAllByMemberId(Long memberId, Pageable pageable);
}
