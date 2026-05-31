package auctionTalk.auction.config.security.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth.frontend-redirect")
public class OAuthFrontendRedirectProperties {

    private String dev;
    private String preview;
    private String prod;

    public String getRedirectUrl(ClientEnv env) {
        return switch (env) {
            case DEV -> dev;
            case PREVIEW -> preview;
            case PROD -> prod;
        };
    }
}