package loyaltycard.controller.dto;

import lombok.ToString;

public  record LoginRequestDto(
        String email,
        String password) {
}
