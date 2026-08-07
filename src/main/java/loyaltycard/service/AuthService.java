package loyaltycard.service;

import loyaltycard.service.model.Customer;

public interface AuthService {

    String signUp(Customer customer);

    String loginCustomer(String email, String password);

    String loginAdmin(String email, String password);
}
