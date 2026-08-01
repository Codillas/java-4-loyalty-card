package loyaltycard.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class SignUpRequestDto {

    private String name;
    private String email;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @ToString.Exclude
    private String password;
}
