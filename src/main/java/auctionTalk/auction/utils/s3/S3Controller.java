package auctionTalk.auction.utils.s3;

import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "S3 API", description = "S3 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "put용 PresignedUrl 발급 API")
    @GetMapping("/presigned/put")
    public BaseResponse<String> getPresignedUrl(
            @RequestParam String category,
            @RequestParam String fileName
    ) {
        return BaseResponse.onSuccess(s3Service.generatePresignedPutUrl(category, fileName));
    }

    @Operation(summary = "get용 PresignedUrl 발급 API")
    @GetMapping("/presigned/get")
    public BaseResponse<String> getPresignedUrl(
            @RequestParam String fileUrl
    ) {
        return BaseResponse.onSuccess(s3Service.generatePresignedGetUrl(fileUrl));
    }
}
