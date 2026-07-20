package com.codillas.loyaltycard.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignUpRequestDto {

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
}
