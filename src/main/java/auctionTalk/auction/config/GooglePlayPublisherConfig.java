package auctionTalk.auction.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "payment.google.enabled", havingValue = "true")
public class GooglePlayPublisherConfig {

    @Value("${payment.google.service-account-path}")
    private Resource serviceAccountResource;

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        try (InputStream is = serviceAccountResource.getInputStream()) {
            return GoogleCredentials.fromStream(is)
                    .createScoped(List.of("https://www.googleapis.com/auth/androidpublisher"));
        }
    }

    @Bean
    public AndroidPublisher androidPublisher(GoogleCredentials googleCredentials) throws Exception {
        return new AndroidPublisher.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(googleCredentials)
        ).setApplicationName("gyeongmaetalk")
                .build();
    }
}