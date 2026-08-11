package loyaltycard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import loyaltycard.controller.dto.CustomerDto;
import loyaltycard.controller.dto.StatusDto;
import loyaltycard.controller.dto.UpdateCustomerRequestDto;
import loyaltycard.exception.CustomerNotFoundException;
import loyaltycard.exception.GlobalExceptionHandler;
import loyaltycard.mapper.CustomerMapper;
import loyaltycard.service.CustomerService;
import loyaltycard.service.model.Customer;
import loyaltycard.service.model.Status;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMVC тести для CustomerController.
 * Використовує standaloneSetup — без Spring Context, тільки контролер + маппер + сервіс.
 */
@ExtendWith(MockitoExtension.class)
class CustomerControllerMockMvcTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CustomerService customerService;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------------------------------------------------------------------------
    // Допоміжні методи
    // ---------------------------------------------------------------------------

    private Customer buildCustomer(UUID id, String name, String email, Status status) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber("+380991234567");
        customer.setStatus(status);
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
        return customer;
    }

    private CustomerDto buildCustomerDto(UUID id, String name, String email, StatusDto status) {
        CustomerDto dto = new CustomerDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        dto.setPhoneNumber("+380991234567");
        dto.setStatusDto(status);
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        return dto;
    }

    // ===========================================================================
    // GET /customers (getAllCustomers)
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | GET /customers → 200 OK зі списком клієнтів")
    void shouldReturn200WithCustomerList() throws Exception {

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Customer c1 = buildCustomer(id1, "Alice", "alice@test.com", Status.ACTIVE);
        Customer c2 = buildCustomer(id2, "Bob", "bob@test.com", Status.ACTIVE);
        CustomerDto dto1 = buildCustomerDto(id1, "Alice", "alice@test.com", StatusDto.ACTIVE);
        CustomerDto dto2 = buildCustomerDto(id2, "Bob", "bob@test.com", StatusDto.ACTIVE);

        when(customerService.getCustomers()).thenReturn(List.of(c1, c2));
        when(customerMapper.toDto(c1)).thenReturn(dto1);
        when(customerMapper.toDto(c2)).thenReturn(dto2);

        // jsonPath("$.length()") — перевіряємо розмір масиву
        // jsonPath("$[0].name") — перший елемент масиву, поле name
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));

        verify(customerService).getCustomers();
    }

    @Test
    @DisplayName("MockMVC | GET /customers → 200 OK з порожнім списком")
    void shouldReturn200WithEmptyList() throws Exception {

        when(customerService.getCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ===========================================================================
    // GET /customers/{customerId}
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | GET /customers/{customerId} → 200 OK з даними клієнта")
    void shouldReturn200WhenGettingCustomerById() throws Exception {

        UUID id = UUID.randomUUID();
        Customer customer = buildCustomer(id, "Alice", "alice@test.com", Status.ACTIVE);
        CustomerDto customerDto = buildCustomerDto(id, "Alice", "alice@test.com", StatusDto.ACTIVE);

        when(customerService.getCustomerById(id)).thenReturn(customer);
        when(customerMapper.toDto(customer)).thenReturn(customerDto);

        mockMvc.perform(get("/customers/{customerId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@test.com"));
    }

    @Test
    @DisplayName("MockMVC | GET /customers/{customerId} → 404 якщо клієнт не існує")
    void shouldReturn404WhenCustomerNotFound() throws Exception {

        UUID id = UUID.randomUUID();
        // GlobalExceptionHandler перетворює виняток у 404 відповідь
        when(customerService.getCustomerById(id)).thenThrow(new CustomerNotFoundException(id));

        mockMvc.perform(get("/customers/{customerId}", id))
                .andExpect(status().isNotFound());
    }

    // ===========================================================================
    // PUT /customers/{customerId}
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | PUT /customers/{customerId} → 200 OK з оновленими даними")
    void shouldReturn200WhenUpdatingCustomer() throws Exception {

        UUID id = UUID.randomUUID();

        UpdateCustomerRequestDto updateDto = new UpdateCustomerRequestDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@test.com");
        updateDto.setPhoneNumber("+380999999999");

        Customer updateDomain = buildCustomer(id, "Updated Name", "updated@test.com", Status.ACTIVE);
        Customer updatedCustomer = buildCustomer(id, "Updated Name", "updated@test.com", Status.ACTIVE);
        CustomerDto updatedDto = buildCustomerDto(id, "Updated Name", "updated@test.com", StatusDto.ACTIVE);

        when(customerMapper.toDomain(any(UpdateCustomerRequestDto.class))).thenReturn(updateDomain);
        when(customerService.updateCustomer(eq(id), any(Customer.class))).thenReturn(updatedCustomer);
        when(customerMapper.toDto(updatedCustomer)).thenReturn(updatedDto);

        // Відправляємо PUT запит з JSON тілом
        mockMvc.perform(
                        put("/customers/{customerId}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));

        verify(customerService).updateCustomer(eq(id), any(Customer.class));
    }

    @Test
    @DisplayName("MockMVC | PUT /customers/{customerId} → 404 якщо клієнт не існує")
    void shouldReturn404WhenUpdatingNonExistentCustomer() throws Exception {

        UUID id = UUID.randomUUID();
        Customer updateDomain = new Customer();
        UpdateCustomerRequestDto updateDto = new UpdateCustomerRequestDto();
        updateDto.setName("Name");
        updateDto.setEmail("e@test.com");
        updateDto.setPhoneNumber("+380991234567");

        when(customerMapper.toDomain(any(UpdateCustomerRequestDto.class))).thenReturn(updateDomain);
        when(customerService.updateCustomer(eq(id), any(Customer.class)))
                .thenThrow(new CustomerNotFoundException(id));

        mockMvc.perform(
                        put("/customers/{customerId}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    // ===========================================================================
    // PUT /customers/{customerId}/activate
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | PUT /customers/{customerId}/activate → 200 OK зі статусом ACTIVE")
    void shouldReturn200WhenActivatingCustomer() throws Exception {

        UUID id = UUID.randomUUID();
        Customer customer = buildCustomer(id, "Alice", "alice@test.com", Status.ACTIVE);
        CustomerDto customerDto = buildCustomerDto(id, "Alice", "alice@test.com", StatusDto.ACTIVE);

        when(customerService.activateCustomer(id)).thenReturn(customer);
        when(customerMapper.toDto(customer)).thenReturn(customerDto);

        mockMvc.perform(put("/customers/{customerId}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusDto").value("ACTIVE"));

        verify(customerService).activateCustomer(id);
    }

    // ===========================================================================
    // PUT /customers/{customerId}/block
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | PUT /customers/{customerId}/block → 200 OK зі статусом BLOCKED")
    void shouldReturn200WhenBlockingCustomer() throws Exception {

        UUID id = UUID.randomUUID();
        Customer customer = buildCustomer(id, "Alice", "alice@test.com", Status.BLOCKED);
        CustomerDto customerDto = buildCustomerDto(id, "Alice", "alice@test.com", StatusDto.BLOCKED);

        when(customerService.blockCustomer(id)).thenReturn(customer);
        when(customerMapper.toDto(customer)).thenReturn(customerDto);

        mockMvc.perform(put("/customers/{customerId}/block", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusDto").value("BLOCKED"));

        verify(customerService).blockCustomer(id);
    }

    @Test
    @DisplayName("MockMVC | PUT /customers/{customerId}/block → 404 якщо клієнт не існує")
    void shouldReturn404WhenBlockingNonExistentCustomer() throws Exception {

        UUID id = UUID.randomUUID();
        when(customerService.blockCustomer(id)).thenThrow(new CustomerNotFoundException(id));

        mockMvc.perform(put("/customers/{customerId}/block", id))
                .andExpect(status().isNotFound());
    }
}
