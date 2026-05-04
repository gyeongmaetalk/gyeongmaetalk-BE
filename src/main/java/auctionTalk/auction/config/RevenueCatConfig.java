package auctionTalk.auction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RevenueCatConfig {

    @Value("${payment.revenue-cat.base-url}")
    private String baseUrl;

    @Value("${payment.revenue-cat.secret-api-key}")
    private String secretApiKey;

    @Bean
    public WebClient revenueCatWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretApiKey)
                .build();
    }
}