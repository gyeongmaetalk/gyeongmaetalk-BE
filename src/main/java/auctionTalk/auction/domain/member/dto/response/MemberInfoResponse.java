package auctionTalk.auction.domain.member.dto.response;

import auctionTalk.auction.domain.member.entity.LoginType;
import auctionTalk.auction.domain.subscription.entity.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MemberInfoResponse {

    @Schema(description = "멤버 이름", example = "임팡수")
    private String name;

    @Schema(description = "로그인 타입", example = "KAKAO")
    private LoginType loginType;

    @Schema(description = "전화번호", example = "01012345678")
    private String cellPhone;

    @Schema(description = "생년월일", example = "1999-10-02")
    private LocalDate birth;

    @Schema(description = "경매 진행 상태", example = "true")
    private SubscriptionStatus auctionStatus;
}
