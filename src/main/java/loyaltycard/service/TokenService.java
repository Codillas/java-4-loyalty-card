package loyaltycard.service;

import loyaltycard.service.model.Role;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface TokenService {

    String createToken(String id, Role role);

    boolean isValidToken(String token);

    String getId(String token);

    Role getRole(String token);

}
