package loyaltycard.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cards")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    private int balance;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StatusEntity statusEntity;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
