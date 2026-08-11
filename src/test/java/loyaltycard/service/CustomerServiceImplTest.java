package loyaltycard.service;

import loyaltycard.exception.CustomerAlreadyExistsException;
import loyaltycard.exception.CustomerNotFoundException;
import loyaltycard.mapper.CustomerMapper;
import loyaltycard.repository.CustomerRepository;
import loyaltycard.repository.entity.CustomerEntity;
import loyaltycard.repository.entity.StatusEntity;
import loyaltycard.service.model.Customer;
import loyaltycard.service.model.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тести для CustomerServiceImpl з Mockito.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    // ---------------------------------------------------------------------------
    // Допоміжні методи
    // ---------------------------------------------------------------------------

    private CustomerEntity buildCustomerEntity(UUID id, String email, StatusEntity status) {
        CustomerEntity entity = new CustomerEntity();
        entity.setId(id);
        entity.setName("Test Customer");
        entity.setEmail(email);
        entity.setPhoneNumber("+380991234567");
        entity.setPassword("encoded_password");
        entity.setStatusEntity(status);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private Customer buildCustomer(UUID id, String email, Status status) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName("Test Customer");
        customer.setEmail(email);
        customer.setPhoneNumber("+380991234567");
        customer.setPassword("encoded_password");
        customer.setStatus(status);
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
        return customer;
    }

    // ===========================================================================
    // createCustomer()
    // ===========================================================================

    @Test
    @DisplayName("createCustomer: успішно створює клієнта, якщо email не зайнятий")
    void shouldCreateCustomerSuccessfully() {

        // given
        UUID id = UUID.randomUUID();
        String email = "customer@test.com";

        Customer inputCustomer = new Customer();
        inputCustomer.setEmail(email);
        inputCustomer.setPassword("plainPassword");

        CustomerEntity savedEntity = buildCustomerEntity(id, email, StatusEntity.ACTIVE);
        Customer expectedCustomer = buildCustomer(id, email, Status.ACTIVE);

        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPassword")).thenReturn("encoded_password");
        when(customerMapper.toEntity(inputCustomer)).thenReturn(savedEntity);
        when(customerRepository.save(savedEntity)).thenReturn(savedEntity);
        when(customerMapper.toDomain(savedEntity)).thenReturn(expectedCustomer);

        // when
        Customer result = customerService.createCustomer(inputCustomer);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(Status.ACTIVE, result.getStatus());
        verify(passwordEncoder).encode("plainPassword");
        verify(customerRepository).save(savedEntity);
    }

    @Test
    @DisplayName("createCustomer: кидає CustomerAlreadyExistsException, якщо email вже зайнятий")
    void shouldThrowWhenEmailAlreadyExists() {

        String email = "exists@test.com";
        Customer inputCustomer = new Customer();
        inputCustomer.setEmail(email);

        CustomerEntity existingEntity = buildCustomerEntity(UUID.randomUUID(), email, StatusEntity.ACTIVE);
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(existingEntity));

        assertThrows(CustomerAlreadyExistsException.class,
                () -> customerService.createCustomer(inputCustomer));
        verify(customerRepository, never()).save(any());
    }

    // ===========================================================================
    // getCustomers()
    // ===========================================================================

    @Test
    @DisplayName("getCustomers: повертає всіх клієнтів")
    void shouldReturnAllCustomers() {

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        CustomerEntity e1 = buildCustomerEntity(id1, "c1@test.com", StatusEntity.ACTIVE);
        CustomerEntity e2 = buildCustomerEntity(id2, "c2@test.com", StatusEntity.ACTIVE);
        Customer c1 = buildCustomer(id1, "c1@test.com", Status.ACTIVE);
        Customer c2 = buildCustomer(id2, "c2@test.com", Status.ACTIVE);

        when(customerRepository.findAll()).thenReturn(List.of(e1, e2));
        when(customerMapper.toDomain(e1)).thenReturn(c1);
        when(customerMapper.toDomain(e2)).thenReturn(c2);

        List<Customer> result = customerService.getCustomers();

        assertEquals(2, result.size());
        verify(customerRepository).findAll();
    }

    // ===========================================================================
    // getCustomerById()
    // ===========================================================================

    @Test
    @DisplayName("getCustomerById: повертає клієнта, якщо він існує")
    void shouldReturnCustomerById() {

        UUID id = UUID.randomUUID();
        CustomerEntity entity = buildCustomerEntity(id, "c@test.com", StatusEntity.ACTIVE);
        Customer expectedCustomer = buildCustomer(id, "c@test.com", Status.ACTIVE);

        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));
        when(customerMapper.toDomain(entity)).thenReturn(expectedCustomer);

        Customer result = customerService.getCustomerById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("getCustomerById: кидає CustomerNotFoundException, якщо клієнта не існує")
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {

        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(id));
    }

    // ===========================================================================
    // updateCustomer()
    // ===========================================================================

    @Test
    @DisplayName("updateCustomer: оновлює поля клієнта і повертає оновлений об'єкт")
    void shouldUpdateCustomerSuccessfully() {

        UUID id = UUID.randomUUID();
        CustomerEntity entity = buildCustomerEntity(id, "old@test.com", StatusEntity.ACTIVE);
        CustomerEntity saved = buildCustomerEntity(id, "new@test.com", StatusEntity.ACTIVE);
        Customer updatedCustomer = buildCustomer(id, "new@test.com", Status.ACTIVE);
        updatedCustomer.setName("New Name");

        Customer update = new Customer();
        update.setName("New Name");
        update.setEmail("new@test.com");
        update.setPhoneNumber("+380999999999");

        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));
        when(customerRepository.save(entity)).thenReturn(saved);
        when(customerMapper.toDomain(saved)).thenReturn(updatedCustomer);

        Customer result = customerService.updateCustomer(id, update);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("new@test.com", result.getEmail());
        verify(customerRepository).save(entity);
    }

    @Test
    @DisplayName("updateCustomer: кидає CustomerNotFoundException, якщо клієнта не існує")
    void shouldThrowWhenUpdatingNonExistentCustomer() {

        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomer(id, new Customer()));
    }

    // ===========================================================================
    // activateCustomer()
    // ===========================================================================

    @Test
    @DisplayName("activateCustomer: встановлює ACTIVE і зберігає клієнта")
    void shouldActivateCustomerSuccessfully() {

        UUID id = UUID.randomUUID();
        CustomerEntity entity = buildCustomerEntity(id, "c@test.com", StatusEntity.BLOCKED);
        CustomerEntity saved = buildCustomerEntity(id, "c@test.com", StatusEntity.ACTIVE);
        Customer expectedCustomer = buildCustomer(id, "c@test.com", Status.ACTIVE);

        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));
        when(customerRepository.save(entity)).thenReturn(saved);
        when(customerMapper.toDomain(saved)).thenReturn(expectedCustomer);

        Customer result = customerService.activateCustomer(id);

        assertEquals(Status.ACTIVE, result.getStatus());
        verify(customerRepository).save(entity);
        assertEquals(StatusEntity.ACTIVE, entity.getStatusEntity());
    }

    // ===========================================================================
    // blockCustomer()
    // ===========================================================================

    @Test
    @DisplayName("blockCustomer: встановлює BLOCKED і зберігає клієнта")
    void shouldBlockCustomerSuccessfully() {

        UUID id = UUID.randomUUID();
        CustomerEntity entity = buildCustomerEntity(id, "c@test.com", StatusEntity.ACTIVE);
        CustomerEntity saved = buildCustomerEntity(id, "c@test.com", StatusEntity.BLOCKED);
        Customer expectedCustomer = buildCustomer(id, "c@test.com", Status.BLOCKED);

        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));
        when(customerRepository.save(entity)).thenReturn(saved);
        when(customerMapper.toDomain(saved)).thenReturn(expectedCustomer);

        Customer result = customerService.blockCustomer(id);

        assertEquals(Status.BLOCKED, result.getStatus());
        verify(customerRepository).save(entity);
        assertEquals(StatusEntity.BLOCKED, entity.getStatusEntity());
    }

    @Test
    @DisplayName("blockCustomer: кидає CustomerNotFoundException, якщо клієнта не існує")
    void shouldThrowWhenBlockingNonExistentCustomer() {

        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.blockCustomer(id));
    }
}
