package auctionTalk.auction.domain.qna.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QnaCreatedEvent {
    private final String memberName;
    private final String title;
    private final String content;
}