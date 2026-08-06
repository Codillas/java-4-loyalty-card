package loyaltycard.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AdminDto {

    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private RoleDto roleDto;
    private StatusDto statusDto;
    private Instant createdAt;
    private Instant updatedAt;

}
