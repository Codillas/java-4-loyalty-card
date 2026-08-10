package loyaltycard.controller;

import loyaltycard.controller.dto.TokenResponseDto;
import loyaltycard.mapper.CustomerMapper;
import loyaltycard.service.AuthService;
import loyaltycard.service.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {


    private static class StubAuthService implements AuthService {

        String lastSignUpToken = "signup-token";
        String lastLoginToken = "login-token";

        @Override
        public String signUp(Customer customer) {
            return lastSignUpToken;
        }

        @Override
        public String loginCustomer(String email, String password) {
            return lastLoginToken;
        }

        @Override
        public String loginAdmin(String email, String password) {
            return "admin-token";
        }
    }

    private AuthController authController;
    private StubAuthService stubAuthService;

    @BeforeEach
    void setUp() {
        stubAuthService = new StubAuthService();
        CustomerMapper customerMapper = new CustomerMapper();
        authController = new AuthController(stubAuthService, customerMapper);
    }

    // -----------------------------------------------------------------------
    // POST /sign-up
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("signUp: returns 201 CREATED and a token in the body")
    void shouldReturnCreatedWhenSignUpRequestIsValid() {

        //given
        var signUpRequest = new loyaltycard.controller.dto.SignUpRequestDto();
        signUpRequest.setName("Vasya Pupkin");
        signUpRequest.setEmail("vasya@mail.com");
        signUpRequest.setPhoneNumber("+380991234567");
        signUpRequest.setPassword("SecurePass123!");
        stubAuthService.lastSignUpToken = "test-jwt-token";

        //when
        ResponseEntity<TokenResponseDto> response = authController.signUp(signUpRequest);

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-jwt-token", response.getBody().getToken());
    }

    @Test
    @DisplayName("signUp: token in body is not null or blank")
    void shouldReturnNonBlankTokenWhenSignUpIsSuccessful() {

        //given
        var signUpRequest = new loyaltycard.controller.dto.SignUpRequestDto();
        signUpRequest.setName("Test User");
        signUpRequest.setEmail("test@mail.com");
        signUpRequest.setPhoneNumber("+380991234568");
        signUpRequest.setPassword("Pass123!");

        //when
        ResponseEntity<TokenResponseDto> response = authController.signUp(signUpRequest);

        //then
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getToken().isBlank());
    }

    // -----------------------------------------------------------------------
    // POST /login
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("login: returns 200 OK and a token in the body")
    void shouldReturnOkWhenLoginCredentialsAreValid() {

        //given
        var loginRequest = new loyaltycard.controller.dto.LoginRequestDto("vasya@mail.com", "SecurePass123!");
        stubAuthService.lastLoginToken = "customer-login-token";

        //when
        ResponseEntity<TokenResponseDto> response = authController.login(loginRequest);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("customer-login-token", response.getBody().getToken());
    }

    @Test
    @DisplayName("login: token in body is not null or blank")
    void shouldReturnNonBlankTokenWhenLoginIsSuccessful() {

        //given
        var loginRequest = new loyaltycard.controller.dto.LoginRequestDto("vasya@mail.com", "Pass123!");

        //when
        ResponseEntity<TokenResponseDto> response = authController.login(loginRequest);

        //then
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getToken().isBlank());
    }
}
