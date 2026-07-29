package loyaltycard.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateCustomerRequestDto {

    private String name;
    private String email;
    private String phoneNumber;
}
