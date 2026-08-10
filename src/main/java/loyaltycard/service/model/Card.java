package loyaltycard.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card {

    private UUID id;
    private UUID customerId;
    private int balance;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;
}
