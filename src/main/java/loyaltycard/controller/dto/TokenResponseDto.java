package loyaltycard.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponseDto {

    private String token;
    private String tokenType;
    private Integer expiresIn;
}
