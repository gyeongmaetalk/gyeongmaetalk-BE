package auctionTalk.auction.config.security.jwt;


import auctionTalk.api.confing.security.auth.PrincipalDetails;
import auctionTalk.api.domain.member.entity.Member;
import auctionTalk.api.domain.member.repository.MemberRepository;
import auctionTalk.api.global.exception.ErrorCode;
import auctionTalk.api.global.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request);

        if (token != null) {
            try {
                if (!jwtTokenProvider.validateToken(token)) {
                    throw new JwtAuthenticationException(ErrorCode.INVALID_JWT_TOKEN);
                }

                Claims claims = jwtTokenProvider.parseClaims(token);

                Long memberId = Long.valueOf(claims.getSubject());

                // 데이터베이스에서 Member 조회
                Member member = memberRepository.getMember(memberId);

                PrincipalDetails principal = new PrincipalDetails(member);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtAuthenticationException e) {
                SecurityContextHolder.clearContext();
                throw e;
            }
        }

        filterChain.doFilter(request, response);
    }

}
