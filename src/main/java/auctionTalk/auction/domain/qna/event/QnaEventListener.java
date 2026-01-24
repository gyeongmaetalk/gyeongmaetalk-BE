package auctionTalk.auction.domain.qna.event;

import auctionTalk.auction.utils.n8n.N8nNotifyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class QnaEventListener {

    private final N8nNotifyClient n8nNotifyClient;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQnaCreated(QnaCreatedEvent event) {
        n8nNotifyClient.sendInquiryMail(
                event.getMemberName(),
                event.getTitle(),
                event.getContent()
        );
    }
}