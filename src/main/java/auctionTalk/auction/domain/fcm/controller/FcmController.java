package auctionTalk.auction.domain.fcm.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.fcm.dto.FcmTokenResponse;
import auctionTalk.auction.domain.fcm.dto.NotificationIdResponse;
import auctionTalk.auction.domain.fcm.dto.NotificationResponse;
import auctionTalk.auction.domain.fcm.service.FcmService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FCM API", description = "알림 관련 API")
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

    @Operation(summary = "알림 목록 조회 API")
    @GetMapping("/notifications")
    public BaseResponse<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal PrincipalDetails principal
    ){
        return BaseResponse.onSuccess(fcmService.getNotifications(principal.getMember()));
    }

    @Operation(summary = " 알림 읽음 처리 API")
    @PatchMapping("/{notificationId}/read")
    public BaseResponse<NotificationIdResponse> readNotification(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable("notificationId") Long notificationId
    ){
        return BaseResponse.onSuccess(fcmService.readNotification(principal.getMember(), notificationId));
    }
}