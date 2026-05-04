package auctionTalk.auction.domain.viewticket.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "내 열람권 잔여 개수 조회 응답")
public class ViewTicketWalletResponse {

    @Schema(description = "잔여 열람권 개수", example = "3")
    private Integer balance;
}