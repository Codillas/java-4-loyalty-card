package loyaltycard.controller;

import loyaltycard.controller.dto.CustomerDto;
import loyaltycard.controller.dto.UpdateCustomerRequestDto;
import loyaltycard.mapper.CustomerMapper;
import loyaltycard.service.CustomerService;
import loyaltycard.service.model.Customer;
import loyaltycard.service.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerControllerTest {

    // --- Stub implementation ---

    private static class StubCustomerService implements CustomerService {

        final UUID fixedId = UUID.fromString("cccccccc-0000-0000-0000-000000000001");

        private Customer buildCustomer(String name, String email, Status status) {
            Customer customer = new Customer();
            customer.setId(fixedId);
            customer.setName(name);
            customer.setEmail(email);
            customer.setPhoneNumber("+380991234567");
            customer.setStatus(status);
            customer.setCreatedAt(Instant.now());
            customer.setUpdatedAt(Instant.now());
            return customer;
        }

        @Override
        public Customer createCustomer(Customer customer) {
            return buildCustomer(customer.getName(), customer.getEmail(), Status.ACTIVE);
        }

        @Override
        public List<Customer> getCustomers() {
            return List.of(
                    buildCustomer("Customer One", "one@mail.com", Status.ACTIVE),
                    buildCustomer("Customer Two", "two@mail.com", Status.BLOCKED)
            );
        }

        @Override
        public Customer getCustomerById(UUID id) {
            return buildCustomer("Customer One", "one@mail.com", Status.ACTIVE);
        }

        @Override
        public Customer updateCustomer(UUID id, Customer customer) {
            return buildCustomer(customer.getName(), customer.getEmail(), Status.ACTIVE);
        }

        @Override
        public Customer activateCustomer(UUID id) {
            return buildCustomer("Customer One", "one@mail.com", Status.ACTIVE);
        }

        @Override
        public Customer blockCustomer(UUID id) {
            return buildCustomer("Customer One", "one@mail.com", Status.BLOCKED);
        }
    }

    private CustomerController customerController;
    private StubCustomerService stubCustomerService;

    @BeforeEach
    void setUp() {
        stubCustomerService = new StubCustomerService();
        CustomerMapper customerMapper = new CustomerMapper();
        customerController = new CustomerController(stubCustomerService, customerMapper);
    }

    // -----------------------------------------------------------------------
    // GET /customers
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getAllCustomers: returns 200 OK with all customers")
    void shouldReturnOkWithListWhenGetAllCustomers() {

        //given — stub always returns 2 customers

        //when
        ResponseEntity<List<CustomerDto>> response = customerController.getAllCustomers();

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("getAllCustomers: response body is not empty")
    void shouldReturnNonEmptyListWhenGetAllCustomers() {

        //given — stub returns a non-empty list

        //when
        ResponseEntity<List<CustomerDto>> response = customerController.getAllCustomers();

        //then
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    // -----------------------------------------------------------------------
    // GET /customers/{customerId}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getCustomerById: returns 200 OK with correct customer")
    void shouldReturnOkWithCustomerWhenCustomerIdIsValid() {

        //given
        UUID customerId = stubCustomerService.fixedId;

        //when
        ResponseEntity<CustomerDto> response = customerController.getCustomerById(customerId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(customerId, response.getBody().getId());
    }

    @Test
    @DisplayName("getCustomerById: returned customer has expected email")
    void shouldReturnCustomerWithExpectedEmailWhenCustomerIdIsValid() {

        //given
        UUID customerId = stubCustomerService.fixedId;

        //when
        ResponseEntity<CustomerDto> response = customerController.getCustomerById(customerId);

        //then
        assertNotNull(response.getBody());
        assertEquals("one@mail.com", response.getBody().getEmail());
    }

    // -----------------------------------------------------------------------
    // PUT /customers/{customerId}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("updateCustomer: returns 200 OK with updated name and email")
    void shouldReturnUpdatedDataWhenUpdateCustomerRequestIsValid() {

        //given
        UUID customerId = stubCustomerService.fixedId;
        UpdateCustomerRequestDto updateRequest = new UpdateCustomerRequestDto();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@mail.com");
        updateRequest.setPhoneNumber("+380991111111");

        //when
        ResponseEntity<CustomerDto> response = customerController.updateCustomer(customerId, updateRequest);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().getName());
        assertEquals("updated@mail.com", response.getBody().getEmail());
    }

    // -----------------------------------------------------------------------
    // PUT /customers/{customerId}/activate
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("activateCustomer: returns 200 OK and status is ACTIVE")
    void shouldReturnActiveStatusWhenCustomerIsActivated() {

        //given
        UUID customerId = stubCustomerService.fixedId;

        //when
        ResponseEntity<CustomerDto> response = customerController.activateCustomer(customerId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(loyaltycard.controller.dto.StatusDto.ACTIVE, response.getBody().getStatusDto());
    }

    // -----------------------------------------------------------------------
    // PUT /customers/{customerId}/block
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("blockCustomer: returns 200 OK and status is BLOCKED")
    void shouldReturnBlockedStatusWhenCustomerIsBlocked() {

        //given
        UUID customerId = stubCustomerService.fixedId;

        //when
        ResponseEntity<CustomerDto> response = customerController.blockCustomer(customerId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(loyaltycard.controller.dto.StatusDto.BLOCKED, response.getBody().getStatusDto());
    }
}
