package auctionTalk.auction.config.security;

import auctionTalk.auction.config.security.auth.AppleClientSecretProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
@RequiredArgsConstructor
public class AppleOAuth2ClientConfig {

    private final AppleClientSecretProvider appleClientSecretProvider;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration apple = ClientRegistration.withRegistrationId("apple")
                .clientId(System.getenv("APPLE_CLIENT_ID"))
                .clientSecret(appleClientSecretProvider.generateClientSecret())
                .clientAuthenticationMethod(new ClientAuthenticationMethod("post"))
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/apple")
                .scope("name", "email")
                .authorizationUri("https://appleid.apple.com/auth/authorize")
                .tokenUri("https://appleid.apple.com/auth/token")
                .userNameAttributeName("sub")
                .clientName("Apple")
                .build();

        return new InMemoryClientRegistrationRepository(apple);
    }
}