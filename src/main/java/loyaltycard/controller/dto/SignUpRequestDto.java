package loyaltycard.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class SignUpRequestDto {

    private String name;
    private String email;
    private String phoneNumber;
    @ToString.Exclude
    private String password;
}
