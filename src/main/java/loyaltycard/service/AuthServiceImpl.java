package loyaltycard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import loyaltycard.exception.AccountIsBlockedException;
import loyaltycard.exception.InvalidCredentialsException;
import loyaltycard.repository.AdminRepository;
import loyaltycard.repository.CustomerRepository;
import loyaltycard.repository.entity.AdminEntity;
import loyaltycard.repository.entity.CustomerEntity;
import loyaltycard.repository.entity.StatusEntity;
import loyaltycard.service.model.Customer;
import loyaltycard.service.model.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountLockedException;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {


    private final CustomerRepository customerRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomerService customerService;
    private final AdminRepository adminRepository;


    @Override
    public String signUp(Customer customer) {

        log.info("Attempting to sign-up customer with email {}", customer.getEmail());

        Customer createdCustomer = customerService.createCustomer(customer);
        String token = tokenService.createToken(createdCustomer.getId().toString(), Role.CUSTOMER);

        log.info("Successfully created the customer with email {}", customer.getEmail());

        return token;
    }

    @Override
    public String loginCustomer(String email, String password) {

        log.info("Attempting to login customer with email {}", email);

        CustomerEntity customerEntity = customerRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        boolean passwordMatches = passwordEncoder.matches(password, customerEntity.getPassword());

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }
        if (customerEntity.getStatusEntity() == StatusEntity.BLOCKED) {
            throw new AccountIsBlockedException();
        }
//        if (!passwordEncoder.matches(password, customerEntity.getPassword())) {
//            throw new InvalidCredentialsException();
//        }
//
        String token = tokenService.createToken(customerEntity.getId().toString(), Role.CUSTOMER);

        log.info("Successfully login the customer with email {}", email);

        return  token;
    }

    @Override
    public String loginAdmin(String email, String password) {

        log.info("Attempting to login admin with email {}", email);

        AdminEntity adminEntity = adminRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        boolean passwordMatches = passwordEncoder.matches(password, adminEntity.getPassword());

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }
        if (adminEntity.getStatusEntity() == StatusEntity.BLOCKED) {
            throw new AccountIsBlockedException();
        }

        String token = tokenService.createToken(adminEntity.getId().toString(), Role.ADMIN);

        log.info("Successfully login the admin with email {}", email);

        return token;
    }
}
