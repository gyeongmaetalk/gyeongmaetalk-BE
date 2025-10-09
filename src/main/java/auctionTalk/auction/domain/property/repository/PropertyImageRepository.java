package auctionTalk.auction.domain.property.repository;

import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {
    List<PropertyImage> findAllByProperty(Property property);

}
