package auctionTalk.auction.domain.property.service;

import auctionTalk.auction.domain.property.entity.Property;
import auctionTalk.auction.domain.property.entity.PropertyImage;
import auctionTalk.auction.domain.property.mapper.PropertyMapper;
import auctionTalk.auction.domain.property.repository.PropertyImageRepository;
import auctionTalk.auction.utils.S3FileComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyImageServiceImpl implements PropertyImageService {

    private final PropertyImageRepository propertyImageRepository;

    private final S3FileComponent s3FileComponent;

    private final PropertyMapper propertyMapper;


    /*
     * 리뷰 이미지 객체를 생성하고 DB에 저장
     */
    @Override
    public List<PropertyImage> createAndSavePropertyImages(Property property, List<MultipartFile> propertyImages) {
        return propertyImages.stream()
                .map(propertyImage -> s3FileComponent.uploadFile("property", propertyImage))
                .map(propertyUrl -> propertyMapper.toPropertyImage(property, propertyUrl))
                .map(propertyImageRepository::save)
                .toList();
    }

    /*
     * 삭제할 기존 리뷰를 S3와 DB에서 삭제
     */
    @Override
    public void deleteExistingImages(List<PropertyImage> imagesToRemove) {
        for (PropertyImage image : imagesToRemove) {
            s3FileComponent.deleteFile(image.getUrl());
            propertyImageRepository.delete(image);
        }
    }

    /*
     * 리뷰 이미지를 업데이트
     */
    @Override
    @Transactional
    public void updatePropertyImages(Property property, List<String> existingImageUrls, List<MultipartFile> newImages) {
        List<PropertyImage> existingImages = propertyImageRepository.findAllByProperty(property);
        List<PropertyImage> existingImagesToKeep = existingImages.stream()
                .filter(image -> existingImageUrls.contains(image.getUrl()))
                .collect(Collectors.toList());

        List<PropertyImage> existingImagesToRemove = existingImages.stream()
                .filter(image -> !existingImageUrls.contains(image.getUrl()))
                .toList();

        // 새로운 이미지 추가
        List<PropertyImage> newPropertyImages = (newImages != null)
                ? createAndSavePropertyImages(property, newImages) : List.of();

        // 유지할 기존 이미지와 새로운 이미지 병합 후 Property에 할당
        existingImagesToKeep.addAll(newPropertyImages);
        property.changeImages(existingImagesToKeep);

        // 삭제할 기존 이미지 삭제
        deleteExistingImages(existingImagesToRemove);
    }
}
