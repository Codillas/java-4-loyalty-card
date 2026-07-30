package loyaltycard.service;

import loyaltycard.service.model.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    List<Customer> getCustomers();

    Customer getCustomerById(UUID id);

    Customer updateCustomer(UUID id, Customer customer);

    Customer activateCustomer(UUID id);

    Customer blockCustomer(UUID id);
}
