package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.entity.PropertyImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PropertyImageService {
    List<PropertyImage> createAndSavePropertyImages(Property property, List<MultipartFile> propertyImages);
    void deleteExistingImages(List<PropertyImage> imagesToRemove);
    void updatePropertyImages(Property property, List<String> existingImageUrls, List<MultipartFile> newImages);
}
