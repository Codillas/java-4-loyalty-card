package loyaltycard.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CustomerDto {

    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;

}
