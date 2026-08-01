package loyaltycard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import loyaltycard.controller.dto.SignUpRequestDto;
import loyaltycard.controller.dto.TokenResponseDto;
import loyaltycard.mapper.CustomerMapper;
import loyaltycard.service.CustomerService;
import loyaltycard.service.TokenService;
import loyaltycard.service.model.Customer;
import loyaltycard.service.model.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final TokenService tokenService;

    @PostMapping("/sign-up")
    public ResponseEntity<TokenResponseDto> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {

        Customer customer = customerMapper.toDomain(signUpRequestDto);
        Customer createdCustomer = customerService.createCustomer(customer);

        String token = tokenService.createToken(createdCustomer.getId().toString(), Role.CUSTOMER);
        TokenResponseDto tokenResponseDto = new TokenResponseDto(token);

        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponseDto);
    }
}

