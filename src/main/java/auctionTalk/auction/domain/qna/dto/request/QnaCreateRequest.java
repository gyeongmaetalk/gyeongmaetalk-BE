package auctionTalk.auction.domain.qna.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QnaCreateRequest {
    private String title;
    private String content;
}
