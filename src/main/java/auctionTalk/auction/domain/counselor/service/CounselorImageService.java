package auctionTalk.auction.domain.counselor.service;

import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.entity.CounselorImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CounselorImageService {
    List<CounselorImage> createAndSaveCounselorImages(Counselor counselor, List<MultipartFile> counselorImages);
    void deleteExistingImages(List<CounselorImage> imagesToRemove);
    void updateCounselorImages(Counselor counselor, List<String> existingImageUrls, List<MultipartFile> newImages);
}
