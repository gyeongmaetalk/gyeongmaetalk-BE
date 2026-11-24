package auctionTalk.auction.config.security.jwt;

import auctionTalk.auction.domain.member.repository.TokenRepository;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import auctionTalk.auction.global.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String MEMBER_ID_KEY = "memberId";
    private static final String BEARER_TYPE = "Bearer";
    // 같은 키로 서명하는 두 토큰을 구분한다. 필터는 access만, 재발급은 refresh만 받는다.
    private static final String TOKEN_TYPE_KEY = "token_type";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 60 * 60 * 1000; // 1시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 15 * 24 * 60 * 60 * 1000; // 15일

    private final Key key;
    private final TokenRepository tokenRepository; // Redis에서 Refresh Token 검증을 위해 추가

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey, TokenRepository tokenRepository) {
        byte[] secretByteKey = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(secretByteKey);
        this.tokenRepository = tokenRepository;
    }

    public JwtToken generateTokens(Long memberId, String role) {
        String accessToken = generateToken(memberId, role, ACCESS_TOKEN_EXPIRE_TIME, ACCESS_TYPE);
        String refreshToken = generateToken(memberId, role, REFRESH_TOKEN_EXPIRE_TIME, REFRESH_TYPE);

        if(memberId != null) {
            tokenRepository.saveRefreshToken(memberId, refreshToken);
        }

        return JwtToken.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String generateToken(
            Long memberId, String role, long expireTime, String tokenType
    ) {
        long now = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireTime))
                .claim(TOKEN_TYPE_KEY, tokenType)
                .signWith(key, SignatureAlgorithm.HS512);

        if (memberId != null) {
            builder.setSubject(String.valueOf(memberId));
        }
        if (memberId != null) {
            builder.claim(MEMBER_ID_KEY, memberId);
        }
        if (role != null) {
            builder.claim("auth", role);
        }

        return builder.compact();
    }

    /**
     * Refresh Token 검증 및 정보 추출 (만료된 경우 예외 발생)
     */
    public RefreshTokenInfo validateAndExtractRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken, REFRESH_TYPE);

        Long memberId = claims.get(MEMBER_ID_KEY, Long.class);

        if (memberId == null) {
            log.error("권한 정보가 없는 Refresh Token입니다.");
            throw new CustomApiException(ErrorCode.UNAUTHORIZED_JWT_TOKEN);
        }

        // Redis에서 저장된 Refresh Token과 비교
        String storedRefreshToken = tokenRepository.getRefreshToken(memberId);
        if (storedRefreshToken == null) {
            log.error("Redis에 해당 Refresh Token이 존재하지 않습니다. memberId: {}", memberId);
            throw new CustomApiException(ErrorCode.EXPIRED_JWT_TOKEN);
        }
        if (!storedRefreshToken.equals(refreshToken)) {
            log.error("Redis에 저장된 Refresh Token과 다릅니다. 위조 가능성 있음. memberId: {}", memberId);
            throw new CustomApiException(ErrorCode.INVALID_JWT_TOKEN);
        }

        return new RefreshTokenInfo(memberId);
    }

    /**
     * 인증 필터용: 서명·만료를 검증하고 액세스 토큰인지 확인한 뒤 Claims를 돌려준다.
     */
    public Claims parseAccessToken(String token) {
        return parseClaims(token, ACCESS_TYPE);
    }

    /**
     * 서명·만료 검증과 Claims 추출을 한 번에 하고, 토큰 용도가 기대한 값인지 확인한다.
     * jjwt 예외는 모두 JwtAuthenticationException으로 바꿔 필터와 컨트롤러가 401로 응답하게 한다.
     */
    private Claims parseClaims(String token, String expectedType) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다. 만료 시각: {}", e.getClaims().getExpiration());
            throw new JwtAuthenticationException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (JwtException e) {
            // 서명 불일치(SignatureException), 형식 오류(MalformedJwtException) 등
            log.warn("잘못된 JWT 토큰: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 문자열이 비어 있습니다.");
            throw new JwtAuthenticationException(ErrorCode.EMPTY_JWT_CLAIMS);
        }

        if (!expectedType.equals(claims.get(TOKEN_TYPE_KEY, String.class))) {
            log.warn("용도가 맞지 않는 JWT 토큰입니다. expected={}, actual={}", expectedType, claims.get(TOKEN_TYPE_KEY));
            throw new JwtAuthenticationException(ErrorCode.INVALID_JWT_TOKEN);
        }
        return claims;
    }


    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}