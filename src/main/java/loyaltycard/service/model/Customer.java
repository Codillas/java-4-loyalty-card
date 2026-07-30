package loyaltycard.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;
}
