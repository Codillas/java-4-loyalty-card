package loyaltycard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import loyaltycard.controller.dto.LoginRequestDto;
import loyaltycard.controller.dto.SignUpRequestDto;
import loyaltycard.exception.AccountIsBlockedException;
import loyaltycard.exception.CustomerAlreadyExistsException;
import loyaltycard.exception.GlobalExceptionHandler;
import loyaltycard.exception.InvalidCredentialsException;
import loyaltycard.mapper.CustomerMapper;
import loyaltycard.service.AuthService;
import loyaltycard.service.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMVC тести для AuthController (публічні endpoints /sign-up та /login).
 * Тестуємо повний HTTP-цикл без Spring Context через standaloneSetup.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerMockMvcTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ===========================================================================
    // POST /sign-up
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | POST /sign-up → 201 CREATED з JWT токеном")
    void shouldReturn201WithTokenWhenSignUpIsSuccessful() throws Exception {

        // given
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setName("New Customer");
        signUpRequest.setEmail("customer@test.com");
        signUpRequest.setPhoneNumber("+380991234567");
        signUpRequest.setPassword("Password1!");

        Customer customer = new Customer();
        customer.setName("New Customer");
        customer.setEmail("customer@test.com");

        // Мокуємо маппер і сервіс
        when(customerMapper.toDomain(any(SignUpRequestDto.class))).thenReturn(customer);
        when(authService.signUp(customer)).thenReturn("jwt-signup-token");

        // when & then
        // perform() — виконати HTTP-запит
        // .content() — тіло запиту (JSON)
        // andExpect(jsonPath("$.token")) — перевіряємо поле token у відповіді
        mockMvc.perform(
                        post("/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())                              // 201
                .andExpect(jsonPath("$.token").value("jwt-signup-token"));   // Поле token у JSON

        verify(authService).signUp(customer);
    }

    @Test
    @DisplayName("MockMVC | POST /sign-up → 400 якщо email вже зайнятий")
    void shouldReturn400WhenEmailAlreadyExists() throws Exception {

        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setName("Customer");
        signUpRequest.setEmail("exists@test.com");
        signUpRequest.setPhoneNumber("+380991234567");
        signUpRequest.setPassword("Password1!");

        Customer customer = new Customer();
        customer.setEmail("exists@test.com");

        when(customerMapper.toDomain(any(SignUpRequestDto.class))).thenReturn(customer);
        // thenThrow — налаштовуємо мок кинути виняток → GlobalExceptionHandler поверне 400
        when(authService.signUp(customer))
                .thenThrow(new CustomerAlreadyExistsException("exists@test.com"));

        mockMvc.perform(
                        post("/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest());  // 400
    }

    // ===========================================================================
    // POST /login
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | POST /login → 200 OK з JWT токеном при коректних кредентіалах")
    void shouldReturn200WithTokenWhenLoginIsSuccessful() throws Exception {

        // given
        LoginRequestDto loginRequest = new LoginRequestDto("customer@test.com", "Password1!");

        when(authService.loginCustomer("customer@test.com", "Password1!"))
                .thenReturn("jwt-login-token");

        // when & then
        mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())                                    // 200
                .andExpect(jsonPath("$.token").value("jwt-login-token"));

        verify(authService).loginCustomer("customer@test.com", "Password1!");
    }

    @Test
    @DisplayName("MockMVC | POST /login → 403 при невірному паролі")
    void shouldReturn403WhenLoginPasswordIsIncorrect() throws Exception {

        LoginRequestDto loginRequest = new LoginRequestDto("customer@test.com", "wrongPass");

        when(authService.loginCustomer("customer@test.com", "wrongPass"))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());  // 403
    }

    @Test
    @DisplayName("MockMVC | POST /login → 403 якщо обліковий запис заблоковано")
    void shouldReturn403WhenAccountIsBlocked() throws Exception {

        LoginRequestDto loginRequest = new LoginRequestDto("blocked@test.com", "Password1!");

        when(authService.loginCustomer("blocked@test.com", "Password1!"))
                .thenThrow(new AccountIsBlockedException());

        mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());  // 403
    }
}
