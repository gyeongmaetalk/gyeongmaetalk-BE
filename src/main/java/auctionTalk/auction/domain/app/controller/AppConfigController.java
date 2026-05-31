package auctionTalk.auction.domain.app.controller;

import auctionTalk.auction.domain.app.dto.response.AppConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "앱 설정 API", description = "앱 설정 관련 API")
public class AppConfigController {

    @Value("${app.review-login-enabled:false}")
    private boolean reviewLoginEnabled;

    @Operation(summary = "앱 설정 조회", description = "앱 실행 시 필요한 설정값을 조회합니다.")
    @GetMapping("/app/config")
    public AppConfigResponse getAppConfig() {
        return new AppConfigResponse(reviewLoginEnabled);
    }
}