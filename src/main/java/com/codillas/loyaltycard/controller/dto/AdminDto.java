package com.codillas.loyaltycard.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class AdminDto {

    private UUID id;
    private String firstName;
    private String LastName;
    private String email;
    private String phoneNumber;
    private Role role;
    private Instant createdAt;
    private Instant updatedAt;

}
