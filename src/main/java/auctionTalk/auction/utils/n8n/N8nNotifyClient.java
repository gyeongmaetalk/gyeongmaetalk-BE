package auctionTalk.auction.utils.n8n;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class N8nNotifyClient {

    private final WebClient webClient;

    @Value("${n8n.webhook.inquiry-notify-url}")
    private String inquiryNotifyUrl;

    public void sendInquiryMail(String memberName, String title, String content) {
        webClient.post()
                .uri(inquiryNotifyUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "memberName", memberName,
                        "title", title,
                        "content", content
                ))
                .retrieve()
                .toBodilessEntity()
                .doOnError(e -> {
                    // 알림 실패는 비즈니스 실패가 아니니까 로그만 남기는 걸 추천
                    System.err.println("[N8N] inquiry notify failed: " + e.getMessage());
                })
                .subscribe();
    }
}
