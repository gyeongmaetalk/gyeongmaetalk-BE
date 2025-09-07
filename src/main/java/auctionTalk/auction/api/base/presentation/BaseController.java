package auctionTalk.auction.api.base.presentation;

import auctionTalk.auction.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/base")
public class BaseController {

    @GetMapping("/health")
    public CommonResponse<String> healthCheck(){
        return CommonResponse.onSuccess("I'm healthy!");
    }
}
