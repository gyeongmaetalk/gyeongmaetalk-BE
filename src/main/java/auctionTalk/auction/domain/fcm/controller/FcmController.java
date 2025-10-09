package auctionTalk.auction.domain.fcm.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.fcm.dto.FcmTokenResponse;
import auctionTalk.auction.domain.fcm.service.FcmService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FCM API", description = "FCM 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FcmController {

    private final FcmService fcmService;

    @Operation(summary = "Fcm 토큰 저장 API")
    @PostMapping("/token")
    @Parameters(value = {
            @Parameter(name = "fcmToken", description = "저장할 FCM 토큰 입력")
    })
    public BaseResponse<FcmTokenResponse> saveFcmToken(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam(name = "fcmToken") String fcmToken
    ){
        return BaseResponse.onSuccess(fcmService.saveFcmToken(principal.getMember().getId(), fcmToken));
    }
}