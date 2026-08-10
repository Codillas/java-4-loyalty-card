package loyaltycard.controller.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDto {

    private UUID id;
    private UUID adminId;
    private UUID cardId;
    private DirectionDto direction;
    private int amount;
    private TransactionStatusDto status;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
}
