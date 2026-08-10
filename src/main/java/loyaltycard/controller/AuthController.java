package loyaltycard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import loyaltycard.controller.dto.LoginRequestDto;
import loyaltycard.controller.dto.SignUpRequestDto;
import loyaltycard.controller.dto.TokenResponseDto;
import loyaltycard.mapper.CustomerMapper;
import loyaltycard.service.AuthService;
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

    private final AuthService authService;
    private final CustomerMapper customerMapper;

    @PostMapping("/sign-up")
    public ResponseEntity<TokenResponseDto> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {

        Customer customer = customerMapper.toDomain(signUpRequestDto);

        String token = authService.signUp(customer);

        TokenResponseDto tokenResponseDto = new TokenResponseDto(token);

        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {

        String token = authService.loginCustomer(loginRequestDto.email(), loginRequestDto.password());
        TokenResponseDto tokenResponseDto = new TokenResponseDto(token);

        return ResponseEntity.status(HttpStatus.OK).body(tokenResponseDto);
    }

}
