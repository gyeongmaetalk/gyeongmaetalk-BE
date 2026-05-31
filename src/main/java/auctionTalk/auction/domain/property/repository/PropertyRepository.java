package auctionTalk.auction.domain.property.repository;

import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    default Property getProperty(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.PROPERTY_NOT_FOUND));
    }

    @Query("""
    select p
    from Property p
    where p.id = :propertyId
      and p.member.id = :memberId
""")
    Optional<Property> findByIdAndMemberId(@Param("propertyId") Long propertyId,
                                           @Param("memberId") Long memberId);

    default Property getPropertyByIdAndMemberId(Long propertyId, Long memberId) {
        return findByIdAndMemberId(propertyId, memberId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.PROPERTY_NOT_FOUND));
    }

    @Query("""
        SELECT p FROM Property p
        WHERE p.member.id = :memberId
          AND (:isPurchased IS NULL OR p.isPurchased = :isPurchased)
    """)
    Page<Property> findAllByMemberIdAndIsPurchased(
            @Param("memberId") Long memberId,
            @Param("isPurchased") Boolean isPurchased,
            Pageable pageable);

    Page<Property> findAllByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable);
}
