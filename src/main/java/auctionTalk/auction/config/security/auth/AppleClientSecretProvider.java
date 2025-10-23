package auctionTalk.auction.config.security.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Component
public class AppleClientSecretProvider {

    @Value("${apple.team-id}")
    private String teamId;
    @Value("${apple.key-id}")
    private String keyId;
    @Value("${apple.client-id}")
    private String clientId;
    @Value("${apple.private-key-path}")
    private String privateKeyPath;

    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        try {
            String key = Files.readString(Path.of(privateKeyPath))
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            privateKey = KeyFactory.getInstance("EC").generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Apple private key", e);
        }
    }

    public String generateClientSecret() {
        Instant now = Instant.now();
        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setIssuer(teamId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(180, ChronoUnit.DAYS)))
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }
}