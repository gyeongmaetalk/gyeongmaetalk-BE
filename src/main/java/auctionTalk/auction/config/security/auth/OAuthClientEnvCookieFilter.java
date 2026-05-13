package auctionTalk.auction.config.security.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

public class OAuthClientEnvCookieFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "oauth_client_env";

    private static final String OAUTH_AUTHORIZATION_URI_PREFIX = "/oauth2/authorization/";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(OAUTH_AUTHORIZATION_URI_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientEnvValue = request.getParameter("client_env");

        if (!ClientEnv.isValid(clientEnvValue)) {
            filterChain.doFilter(request, response);
            return;
        }

        ClientEnv clientEnv = ClientEnv.fromOrDefault(clientEnvValue, ClientEnv.DEV);

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, clientEnv.name())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMinutes(5))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        filterChain.doFilter(request, response);
    }
}