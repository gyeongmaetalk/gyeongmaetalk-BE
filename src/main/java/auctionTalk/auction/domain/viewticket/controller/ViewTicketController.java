package auctionTalk.auction.domain.viewticket.controller;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.viewticket.dto.response.ViewTicketPurchasePageResponse;
import auctionTalk.auction.domain.viewticket.dto.response.ViewTicketWalletResponse;
import auctionTalk.auction.domain.viewticket.service.ViewTicketService;
import auctionTalk.auction.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/view-tickets")
@Tag(name = "매물 열람권 API", description = "매물 열람권 관련 API")
public class ViewTicketController {

    private final ViewTicketService viewTicketService;


    @GetMapping("/purchase-page")
    @Operation(
            summary = "열람권 구매 화면 조회",
            description = "판매 중인 열람권 상품 목록과 로그인한 회원의 잔여 열람권 개수를 조회합니다."
    )
    public BaseResponse<ViewTicketPurchasePageResponse> getPurchasePage(
            @AuthenticationPrincipal(errorOnInvalidType = true) PrincipalDetails principal
    ) {
        return BaseResponse.onSuccess(
                viewTicketService.getPurchasePage(principal.getMember())
        );
    }

    @GetMapping("/me/wallet")
    @Operation(
            summary = "내 잔여 열람권 조회",
            description = "로그인한 회원의 잔여 매물 열람권 개수를 조회합니다."
    )
    public BaseResponse<ViewTicketWalletResponse> getMyWallet(
            @AuthenticationPrincipal(errorOnInvalidType = true) PrincipalDetails principal
    ) {
        return BaseResponse.onSuccess(
                viewTicketService.getMyWallet(principal.getMember())
        );
    }
}
