package auctionTalk.auction.domain.qna.dto.request;

import auctionTalk.auction.domain.qna.entity.QnaCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QnaCreateRequest {
    private String title;
    private String content;
    private QnaCategory category;
}
