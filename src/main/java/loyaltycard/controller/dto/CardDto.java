package loyaltycard.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CardDto {

    private UUID id;
    private UUID customerId;
    private int balance;
    private StatusDto statusDto;
    private Instant createdAt;
    private Instant updatedAt;
}
