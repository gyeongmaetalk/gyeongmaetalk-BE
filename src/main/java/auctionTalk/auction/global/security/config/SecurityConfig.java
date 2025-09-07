package auctionTalk.auction.global.security.config;

import auctionTalk.auction.global.security.filter.JwtAuthFilter;
import auctionTalk.auction.global.security.handler.JwtAccessDeniedHandler;
import auctionTalk.auction.global.security.handler.JwtAuthenticationEntryPoint;
import auctionTalk.auction.global.security.handler.JwtAuthenticationExceptionHandler;
import auctionTalk.auction.global.security.provider.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
//@EnableWebSecurity(debug = true)
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint();

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler = new JwtAccessDeniedHandler();

    private final TokenProvider tokenProvider;

    private final JwtAuthenticationExceptionHandler jwtAuthenticationExceptionHandler =
            new JwtAuthenticationExceptionHandler();

    private static final String[] whiteList = {
            "/schedule",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/favicon.io",
            "/favicon.ico",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/docs/**","/base/health","/error"
    };

    /**
     * 특정 경로에 대한 보안 설정을 무시하도록 설정
     * @return WebSecurityCustomizer
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                web.ignoring()
                        .requestMatchers(
                                "/base/health",
                                "/schedule",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/favicon.io",
                                "/favicon.ico",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/docs/**");
    }

    @Bean
    public SecurityFilterChain JwtFilterChain(HttpSecurity http) throws Exception {
        return http.cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfiguration()))
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // 비활성화
                .sessionManagement(
                        manage ->
                                manage.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS)) // Session 사용 안함
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize -> {
//                            authorize.requestMatchers("/swagger-ui/**").permitAll();
                            authorize.requestMatchers("/api/v1/users/auth/**").permitAll();
                            authorize.requestMatchers("/error").permitAll();
                            authorize.anyRequest().authenticated();
                        })
                .exceptionHandling(
                        exceptionHandling ->
                                exceptionHandling
                                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .addFilterBefore(
                        new JwtAuthFilter(tokenProvider, whiteList),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationExceptionHandler, JwtAuthFilter.class)
                .build();
    }

    public CorsConfigurationSource corsConfiguration() {
        return request -> {
            org.springframework.web.cors.CorsConfiguration config =
                    new org.springframework.web.cors.CorsConfiguration();
            config.setAllowedHeaders(Collections.singletonList("*")); // 모든 헤더 허용
            config.setAllowedMethods(Collections.singletonList("*")); // 모든 메소드 허용
            config.setAllowedOriginPatterns(Collections.singletonList("*")); // 모든 Origin 허용
            config.setAllowCredentials(true);   // 인증정보 허용
            return config;
        };
    }
}

