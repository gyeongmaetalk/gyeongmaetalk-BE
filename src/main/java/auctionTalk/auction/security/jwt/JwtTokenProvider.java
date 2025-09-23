package auctionTalk.auction.security.jwt;


import auctionTalk.api.domain.member.repository.TokenRepository;
import auctionTalk.api.global.exception.CustomApiException;
import auctionTalk.api.global.exception.ErrorCode;
import auctionTalk.api.global.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String MEMBER_ID_KEY = "memberId";
    private static final String BEARER_TYPE = "Bearer";

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
        String accessToken = generateToken(memberId, role, ACCESS_TOKEN_EXPIRE_TIME);
        String refreshToken = generateToken(memberId, role, REFRESH_TOKEN_EXPIRE_TIME);

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
            Long memberId, String role, long expireTime
    ) {
        long now = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireTime))
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
        Claims claims = parseClaims(refreshToken);

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

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 토큰: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException e) {
            Claims expiredClaims = e.getClaims();
            Date exp = expiredClaims.getExpiration();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.info("access 토큰 만료일자 : {}", sdf.format(exp));
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
            throw new JwtAuthenticationException(ErrorCode.EXPIRED_JWT_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 토큰: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            log.error("JWT Claims가 비어 있음: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.EMPTY_JWT_CLAIMS);
        }
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰, Claims 추출 불가: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.EXPIRED_JWT_TOKEN);
        }
    }


    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}