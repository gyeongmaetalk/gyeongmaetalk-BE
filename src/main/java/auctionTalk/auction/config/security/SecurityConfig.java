package auctionTalk.auction.config.security;

import auctionTalk.auction.config.security.jwt.CustomAccessDeniedHandler;
import auctionTalk.auction.config.security.jwt.CustomAuthenticationEntryPoint;
import auctionTalk.auction.config.security.jwt.JwtAuthorizationFilter;
import auctionTalk.auction.config.security.jwt.JwtTokenProvider;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstant.PUBLIC_URLS).permitAll() // 비로그인 허용 API
                        .requestMatchers(SecurityConstant.AUTHENTICATED_URLS).authenticated() // 로그인 필요
                        .requestMatchers(SecurityConstant.ADMIN_URLS).hasRole("ADMIN") // 관리자 권한
                        .anyRequest().authenticated() // 기타 모든 요청은 인증 필요
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 인증 실패 처리
                        .accessDeniedHandler(customAccessDeniedHandler)           // 인가 실패 처리
                )
                .addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtTokenProvider, memberRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }
}