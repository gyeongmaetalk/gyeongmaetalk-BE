package auctionTalk.auction.config.security;

import auctionTalk.auction.config.security.auth.AppleClientSecretProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class AppleOAuth2ClientConfig {

    private final AppleClientSecretProvider appleClientSecretProvider;

    @Value("${apple.client-id}")
    private String clientId;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {


        List<ClientRegistration> registrations = properties.getRegistration().keySet().stream()
                .map(client -> getRegistration(properties, client))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        ClientRegistration apple = ClientRegistration.withRegistrationId("apple")
                .clientId(clientId)
                .clientSecret(appleClientSecretProvider.generateClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/apple")
                .scope("name", "email")
                .authorizationUri("https://appleid.apple.com/auth/authorize?response_mode=form_post")
                .tokenUri("https://appleid.apple.com/auth/token")
                .userNameAttributeName("sub")
                .clientName("Apple")
                .build();

        registrations.add(apple);

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration getRegistration(OAuth2ClientProperties properties, String client) {
        OAuth2ClientProperties.Registration registration = properties.getRegistration().get(client);
        if (registration == null) return null;

        OAuth2ClientProperties.Provider provider = properties.getProvider().get(client);
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(client)
                .clientId(registration.getClientId())
                .clientSecret(registration.getClientSecret())
                .authorizationGrantType(new AuthorizationGrantType(registration.getAuthorizationGrantType()))
                .redirectUri(registration.getRedirectUri())
                .scope(registration.getScope())
                .clientName(client);

        if (provider != null) {
            if (provider.getAuthorizationUri() != null)
                builder.authorizationUri(provider.getAuthorizationUri());
            if (provider.getTokenUri() != null)
                builder.tokenUri(provider.getTokenUri());
            if (provider.getUserInfoUri() != null)
                builder.userInfoUri(provider.getUserInfoUri());
            if (provider.getUserNameAttribute() != null)
                builder.userNameAttributeName(provider.getUserNameAttribute());
        }

        return builder.build();
    }
}