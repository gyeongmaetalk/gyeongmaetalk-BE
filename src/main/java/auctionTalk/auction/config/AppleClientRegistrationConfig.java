package auctionTalk.auction.config;

import auctionTalk.auction.config.security.auth.AppleClientSecretProvider;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AppleClientRegistrationConfig {

    private final AppleClientSecretProvider appleClientSecretProvider;

    public AppleClientRegistrationConfig(AppleClientSecretProvider appleClientSecretProvider) {
        this.appleClientSecretProvider = appleClientSecretProvider;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
        List<ClientRegistration> registrations = new ArrayList<>();

        properties.getRegistration().forEach((id, reg) -> {
            OAuth2ClientProperties.Provider provider = properties.getProvider().get(id);

            ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(id)
                    .clientId(reg.getClientId())
                    .clientSecret(
                            "apple".equals(id)
                                    ? appleClientSecretProvider.generateClientSecret()
                                    : reg.getClientSecret()
                    )
                    .clientAuthenticationMethod(
                            new ClientAuthenticationMethod(reg.getClientAuthenticationMethod() != null
                                    ? reg.getClientAuthenticationMethod()
                                    : "client_secret_post"))
                    .authorizationGrantType(
                            new AuthorizationGrantType(reg.getAuthorizationGrantType() != null
                                    ? reg.getAuthorizationGrantType()
                                    : "authorization_code"))
                    .redirectUri(reg.getRedirectUri())
                    .scope(reg.getScope())
                    .authorizationUri(provider.getAuthorizationUri())
                    .tokenUri(provider.getTokenUri())
                    .jwkSetUri(provider.getJwkSetUri())
                    .userNameAttributeName(provider.getUserNameAttribute());

            registrations.add(builder.build());
        });

        return new InMemoryClientRegistrationRepository(registrations);
    }
}