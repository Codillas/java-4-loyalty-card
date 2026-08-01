package loyaltycard.service;

import loyaltycard.service.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {

    private static final String CLAIM_ROLE = "role";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.ttl-millis}")
    private Long jwtTtlMillis;

    @Override
    public String createToken(String id, Role role) {

        // Calculate the expiration date based on the current time and expiration time in milliseconds
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtTtlMillis);

        // Build JWT claims
        Claims claims =
                Jwts.claims()
                        .issuedAt(now)
                        .expiration(expiration)
                        .subject(id)
                        .add(CLAIM_ROLE, role.toString())
                        .build();

        // Create and sign the JWT token
        return Jwts.builder().claims(claims).signWith(getSecretKey()).compact();
    }

    @Override
    public boolean isValidToken(String token) {

        try {
            Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public String getId(String token) {

        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @Override
    public Role getRole(String token) {

        String typeValue =
                Jwts.parser()
                        .verifyWith(getSecretKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .get(CLAIM_ROLE, String.class);

        return Role.valueOf(typeValue);
    }

    private SecretKey getSecretKey() {

        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}