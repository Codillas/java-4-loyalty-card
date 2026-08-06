package loyaltycard.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admins")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String password;

    @Enumerated(EnumType.STRING)
    private RoleEntity roleEntity;

    @Enumerated(EnumType.STRING)
    private StatusEntity statusEntity;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    }



