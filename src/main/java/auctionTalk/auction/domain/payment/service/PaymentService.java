package auctionTalk.auction.domain.payment.service;

import auctionTalk.auction.domain.payment.dto.request.PaymentConfirmRequest;
import auctionTalk.auction.domain.payment.dto.request.PaymentRefundRequest;
import auctionTalk.auction.domain.payment.dto.response.PaymentResultResponse;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final WebClient webClient;

    @Value("${toss.secret-key}")
    private String secretKey;
    @Value("${toss.payments-confirm-url}")
    private String tossConfirmUrl;
    @Value("${toss.payments-cancel-url}")
    private String tossCancelUrl;

    /*
     * 결제 승인
     */
    @Transactional
    public PaymentResultResponse callTossPaymentApi(PaymentConfirmRequest request) {

        Map<String, Object> body = Map.of(
                "paymentKey", request.getPaymentKey(),
                "orderId", request.getOrderId(),
                "amount", request.getAmount()
        );

        return webClient.post()
                .uri(tossConfirmUrl)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Toss payment confirm failed: {}", errorBody);
                                    return Mono.error(new CustomApiException(ErrorCode.FAIL_CONFIRM_PAYMENT));
                                })
                )
                .bodyToMono(PaymentResultResponse.class)
                .block();
    }

    /*
     * 결제 환불
     */
    @Transactional
    public PaymentResultResponse refundPayment(PaymentRefundRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", request.getCancelReason());

        // 부분 환불일 경우만
        if (request.getCancelAmount() != null) {
            body.put("cancelAmount", request.getCancelAmount());
        }

        return webClient.post()
                .uri(tossCancelUrl + "/" + request.getPaymentKey() + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> {
                                    log.error("Toss refund failed: {}", error);
                                    return Mono.error(new CustomApiException(ErrorCode.FAIL_CANCEL_PAYMENT));
                                })
                )
                .bodyToMono(PaymentResultResponse.class)
                .block();
    }

    private String getAuthHeader() {
        return "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }
}
