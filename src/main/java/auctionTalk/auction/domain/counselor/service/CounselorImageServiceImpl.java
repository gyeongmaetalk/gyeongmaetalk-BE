package auctionTalk.auction.domain.counselor.service;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.entity.CounselorImage;
import auctionTalk.auction.domain.counselor.mapper.CounselorMapper;
import auctionTalk.auction.domain.counselor.repository.CounselorImageRepository;
import auctionTalk.auction.utils.S3FileComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselorImageServiceImpl implements CounselorImageService {

    private final CounselorImageRepository counselorImageRepository;

    private final S3FileComponent s3FileComponent;

    private final CounselorMapper counselorMapper;

    /*
     * 리뷰 이미지 객체를 생성하고 DB에 저장
     */
    @Override
    public List<CounselorImage> createAndSaveCounselorImages(Counselor counselor, List<MultipartFile> counselorImages) {
        return counselorImages.stream()
                .map(counselorImage -> s3FileComponent.uploadFile("counselor", counselorImage))
                .map(counselorUrl -> counselorMapper.toCounselorImage(counselor, counselorUrl))
                .map(counselorImageRepository::save)
                .toList();
    }

    /*
     * 삭제할 기존 리뷰를 S3와 DB에서 삭제
     */
    @Override
    public void deleteExistingImages(List<CounselorImage> imagesToRemove) {
        for (CounselorImage image : imagesToRemove) {
            s3FileComponent.deleteFile(image.getUrl());
            counselorImageRepository.delete(image);
        }
    }

    /*
     * 리뷰 이미지를 업데이트
     */
    @Override
    @Transactional
    public void updateCounselorImages(Counselor counselor, List<String> existingImageUrls, List<MultipartFile> newImages) {
        List<CounselorImage> existingImages = counselorImageRepository.findAllByCounselor(counselor);
        List<CounselorImage> existingImagesToKeep = existingImages.stream()
                .filter(image -> existingImageUrls.contains(image.getUrl()))
                .collect(Collectors.toList());

        List<CounselorImage> existingImagesToRemove = existingImages.stream()
                .filter(image -> !existingImageUrls.contains(image.getUrl()))
                .toList();

        // 새로운 이미지 추가
        List<CounselorImage> newCounselorImages = (newImages != null)
                ? createAndSaveCounselorImages(counselor, newImages) : List.of();

        // 유지할 기존 이미지와 새로운 이미지 병합 후 Counselor에 할당
        existingImagesToKeep.addAll(newCounselorImages);
        counselor.changeImages(existingImagesToKeep);

        // 삭제할 기존 이미지 삭제
        deleteExistingImages(existingImagesToRemove);
    }
}
