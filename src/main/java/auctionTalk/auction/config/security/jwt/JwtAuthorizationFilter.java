package auctionTalk.auction.config.security.jwt;

import auctionTalk.auction.config.security.auth.PrincipalDetails;
import auctionTalk.auction.domain.member.entity.Member;
import auctionTalk.auction.domain.member.repository.MemberRepository;
import auctionTalk.auction.global.common.BaseResponse;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.global.exception.JwtAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();


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

                PrincipalDetails principal = new PrincipalDetails(member, null);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtAuthenticationException e) {
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, e.getErrorCode());
                return;
            } catch (CustomApiException e) {
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, e.getErrorCode());
                return;
            }


        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        BaseResponse<Object> errorResponse = BaseResponse.onFailure(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}
