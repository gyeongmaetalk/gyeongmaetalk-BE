package auctionTalk.auction.config.security;

import auctionTalk.auction.config.security.auth.*;
import auctionTalk.auction.config.security.jwt.CustomAccessDeniedHandler;
import auctionTalk.auction.config.security.jwt.CustomAuthenticationEntryPoint;
import auctionTalk.auction.config.security.jwt.JwtAuthorizationFilter;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final AppleOidcUserService appleOidcUserService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2AuthorizationRequestResolver authorizationRequestResolver) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {}) // 기본 설정 = CorsConfig의 corsConfigurationSource 빈 사용
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 규칙은 위에서부터 처음 매칭되는 하나만 적용된다.
                // 넓은 규칙이 좁은 규칙을 가리지 않도록 관리자 규칙과 예외 규칙을 먼저 둔다.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstant.PUBLIC_URLS).permitAll()
                        .requestMatchers(SecurityConstant.ADMIN_URLS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, SecurityConstant.ADMIN_POST_URLS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, SecurityConstant.ADMIN_PATCH_URLS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, SecurityConstant.AUTHENTICATED_GET_URLS).authenticated()
                        .requestMatchers(HttpMethod.GET, SecurityConstant.PUBLIC_GET_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 인증 실패 처리
                        .accessDeniedHandler(customAccessDeniedHandler)           // 인가 실패 처리
                )
                .addFilterBefore(oAuthClientEnvCookieFilter(), OAuth2AuthorizationRequestRedirectFilter.class)
                .addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(ae -> ae
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                                .authorizationRequestResolver(authorizationRequestResolver)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 사용자 정보 로드
                                .oidcUserService(appleOidcUserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // 로그인 성공 시 JWT 발급
                        .failureHandler((request, response, exception) -> {
                            System.err.println("❌ OAuth2 로그인 실패: " + exception.getMessage());
                            exception.printStackTrace();
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth2 로그인 실패: " + exception.getMessage());
                        })
                );

        return http.build();
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtTokenProvider, memberRepository);
    }

    @Bean
    public OAuthClientEnvCookieFilter oAuthClientEnvCookieFilter() {
        return new OAuthClientEnvCookieFilter();
    }

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository repo) {

        DefaultOAuth2AuthorizationRequestResolver delegate =
                new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest req = delegate.resolve(request);
                return customize(req);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
                OAuth2AuthorizationRequest req = delegate.resolve(request, registrationId);
                return customize(req);
            }

            private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req) {
                if (req == null) return null;

                // registrationId 꺼내기
                String regId = (String) req.getAttributes().get(OAuth2ParameterNames.REGISTRATION_ID);
                if (!"apple".equals(regId)) return req;

                // response_mode=form_post 추가
                Map<String, Object> extra = new HashMap<>(req.getAdditionalParameters());
                extra.put("response_mode", "form_post");

                return OAuth2AuthorizationRequest.from(req)
                        .additionalParameters(extra)
                        .build();
            }
        };
    }
}
