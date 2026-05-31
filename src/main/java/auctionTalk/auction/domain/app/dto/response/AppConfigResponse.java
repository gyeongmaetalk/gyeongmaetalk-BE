package auctionTalk.auction.domain.app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "앱 설정 응답")
public record AppConfigResponse(

        @Schema(description = "심사용 로그인 버튼 노출 여부", example = "true")
        boolean reviewLoginEnabled
) {
}