package loyaltycard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import loyaltycard.exception.CustomerAlreadyExistsException;
import loyaltycard.exception.CustomerNotFoundException;
import loyaltycard.mapper.CustomerMapper;
import loyaltycard.repository.CustomerRepository;
import loyaltycard.repository.entity.CustomerEntity;
import loyaltycard.service.model.Customer;
import loyaltycard.service.model.Status;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public Customer createCustomer(Customer customer) {

        log.info("Attempting to create a customer with email {}", customer.getEmail());

        Optional<CustomerEntity> optionalCustomer = customerRepository.findByEmail(customer.getEmail());

        if (optionalCustomer.isPresent()) {
            throw new CustomerAlreadyExistsException(customer.getEmail());
        }

        customer.setStatus(Status.ACTIVE);
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());

        CustomerEntity customerEntity = customerMapper.toEntity(customer);

        CustomerEntity savedCustomer = customerRepository.save(customerEntity);

        log.info("Successfully created a customer with email {}", customer.getEmail());

        return customerMapper.toDomain(savedCustomer);
    }

    @Override
    public List<Customer> getCustomers() {

        List<CustomerEntity> customerEntityList = customerRepository.findAll();

        List<Customer> customerList = customerEntityList.stream().
                map(customerEntity -> customerMapper.toDomain(customerEntity))
                .toList();

        return customerList;
    }

    @Override
    public Customer getCustomerById(UUID customerId) {

        Optional<CustomerEntity> optionalCustomer = customerRepository.findById(customerId);

        if (optionalCustomer.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }

        CustomerEntity customerEntity = optionalCustomer.get();
        Customer customer = customerMapper.toDomain(customerEntity);

        return customer;
    }

    @Override
    public Customer updateCustomer(UUID customerId, Customer customer) {

        log.info("Attempting to update customer with id {}", customerId);

        Optional<CustomerEntity> optionalCustomer = customerRepository.findById(customerId);

        if (optionalCustomer.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }

        CustomerEntity customerEntity = optionalCustomer.get();
        customerEntity.setName(customer.getName());
        customerEntity.setEmail(customer.getEmail());
        customerEntity.setPhoneNumber(customer.getPhoneNumber());
        customerEntity.setUpdatedAt(Instant.now());

        CustomerEntity savedCustomer = customerRepository.save(customerEntity);

        log.info("Successfully updated customer with id {}", customerId);

        return customerMapper.toDomain(savedCustomer);
    }

    @Override
    public Customer activateCustomer(UUID customerId) {

        Optional<CustomerEntity> optionalCustomer = customerRepository.findById(customerId);

        if (optionalCustomer.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }

        CustomerEntity customerEntity = optionalCustomer.get();
        customerEntity.setStatus(loyaltycard.repository.entity.Status.ACTIVE);
        customerEntity.setUpdatedAt(Instant.now());
        CustomerEntity savedCustomer = customerRepository.save(customerEntity);
        Customer customer = customerMapper.toDomain(savedCustomer);

        return customer;
    }

    @Override
    public Customer blockCustomer(UUID customerId) {

        Optional<CustomerEntity> optionalCustomer = customerRepository.findById(customerId);

        if (optionalCustomer.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }

        CustomerEntity customerEntity = optionalCustomer.get();
        customerEntity.setStatus(loyaltycard.repository.entity.Status.BLOCKED);
        customerEntity.setUpdatedAt(Instant.now());
        CustomerEntity savedCustomer = customerRepository.save(customerEntity);
        Customer customer = customerMapper.toDomain(savedCustomer);

        return customer;
    }

}
