package loyaltycard.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JwtService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final int EXPIRES_IN_SECONDS = 3600;

    public String generateToken(String email) {
        return "mock-jwt-" + UUID.randomUUID();
    }

    public String getTokenType() {
        return TOKEN_TYPE;
    }

    public int getExpiresIn() {
        return EXPIRES_IN_SECONDS;
    }
}
