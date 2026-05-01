package auctionTalk.auction.config;

import com.apple.itunes.storekit.model.Environment;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class AppleStoreClientConfig {

    @Value("${payment.apple.bundle-id}")
    private String bundleId;

    @Value("${payment.apple.app-apple-id:#{null}}")
    private Long appAppleId;

    @Value("${payment.apple.environment}")
    private String environment;

    @Value("${payment.apple.root-certificates}")
    private Resource[] rootCertificates;

    @Bean
    public SignedDataVerifier signedDataVerifier() throws Exception {
        Set<InputStream> rootCAs = new HashSet<>();
        for (Resource resource : rootCertificates) {
            rootCAs.add(resource.getInputStream());
        }

        return new SignedDataVerifier(
                rootCAs,
                bundleId,
                appAppleId,
                Environment.valueOf(environment),
                true
        );
    }
}