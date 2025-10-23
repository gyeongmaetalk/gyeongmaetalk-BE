package auctionTalk.auction.config.security;

import auctionTalk.auction.config.security.auth.AppleClientSecretProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${apple.client-id}")
    private String clientId;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration apple = ClientRegistration.withRegistrationId("apple")
                .clientId(clientId)
                .clientSecret(appleClientSecretProvider.generateClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
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