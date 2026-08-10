package loyaltycard.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    private UUID id;
    private UUID adminId;
    private UUID cardId;
    private Direction direction;
    private int amount;
    private TransactionStatus status;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
}
