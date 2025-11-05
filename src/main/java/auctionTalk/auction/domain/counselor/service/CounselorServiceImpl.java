package auctionTalk.auction.domain.counselor.service;

import auctionTalk.auction.domain.counsel.entity.Counsel;
import auctionTalk.auction.domain.counsel.repository.CounselRepository;
import auctionTalk.auction.domain.counselor.dto.request.CounselorCreateRequest;
import auctionTalk.auction.domain.counselor.dto.request.CounselorUpdateRequest;
import auctionTalk.auction.domain.counselor.dto.response.CounselorIdResponse;
import auctionTalk.auction.domain.counselor.dto.response.CounselorResponse;
import auctionTalk.auction.domain.counselor.entity.Counselor;
import auctionTalk.auction.domain.counselor.mapper.CounselorMapper;
import auctionTalk.auction.domain.counselor.repository.CounselorRepository;
import auctionTalk.auction.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselorServiceImpl implements CounselorService {

    private final CounselorMapper counselorMapper;
    private final CounselorRepository counselorRepository;
    private final CounselRepository counselRepository;
//    private final CounselorImageService counselorImageService;

    @Override
    @Transactional
    public CounselorIdResponse createCounselor(CounselorCreateRequest counselorCreateRequest, List<MultipartFile> counselorImages){

        Counselor newCounselor = counselorMapper.toCounselor(counselorCreateRequest);
        counselorRepository.save(newCounselor);

//        if (counselorImages != null) {
//            List<CounselorImage> newCounselorImages = counselorImageService.createAndSaveCounselorImages(newCounselor, counselorImages);
//            newCounselor.changeImages(newCounselorImages);
//        }

        return new CounselorIdResponse(newCounselor.getId());
    }

    @Override
    @Transactional
    public CounselorIdResponse updateCounselor(Long counselorId, CounselorUpdateRequest request, List<MultipartFile> newImages){

        Counselor counselor =  counselorRepository.getCounselor(counselorId);
        counselor.updateCounselorInfo(request);

//        if (newImages != null && !newImages.isEmpty()) {
//            counselorImageService.updateCounselorImages(counselor, request.getExistingImages(), newImages);
//        }

        return new CounselorIdResponse(counselorId);
    }

    @Override
    @Transactional
    public CounselorResponse inquiryCounselor(Long counselorId, Member member){
        Counselor counselor = counselorRepository.getCounselor(counselorId);
        Counsel counsel = counselRepository.getCounselByMember(member);

        LocalDateTime counselDateTime = LocalDateTime.of(
                counsel.getCounselDate(),
                counsel.getCounselTime()
        );

        return counselorMapper.toCounselorResponse(counselor,counselDateTime);
    }
}
