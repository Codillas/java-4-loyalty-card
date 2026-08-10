package loyaltycard.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "admin_id")
    private UUID adminId;

    @Column(name = "card_id")
    private UUID cardId;

    @Column(name = "direction")
    @Enumerated(EnumType.STRING)
    private DirectionEntity direction;

    private int amount;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TransactionStatusEntity status;

    private String note;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
