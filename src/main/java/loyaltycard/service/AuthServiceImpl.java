package loyaltycard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import loyaltycard.exception.AccountIsBlockedException;
import loyaltycard.exception.InvalidCredentialsException;
import loyaltycard.mapper.AdminMapper;
import loyaltycard.mapper.CustomerMapper;
import loyaltycard.repository.AdminRepository;
import loyaltycard.repository.CustomerRepository;
import loyaltycard.repository.entity.AdminEntity;
import loyaltycard.repository.entity.CustomerEntity;
import loyaltycard.service.model.Admin;
import loyaltycard.service.model.Customer;
import loyaltycard.service.model.Role;
import loyaltycard.service.model.Status;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {


    private final CustomerRepository customerRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomerService customerService;
    private final AdminRepository adminRepository;
    private final CustomerMapper customerMapper;
    private final AdminMapper adminMapper;


    @Override
    public String signUp(Customer customer) {

        log.info("Attempting to sign-up customer with email {}", customer.getEmail());

        // User createdUser = userService.createUser(user)
        //String token = tokenService.createToken(createdUser.getId().toString(), Role.WORKER);
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
        Customer customer = customerMapper.toDomain(customerEntity);

        boolean passwordMatches = passwordEncoder.matches(password, customer.getPassword());

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }
        if (customer.getStatus() == Status.BLOCKED) {
            throw new AccountIsBlockedException();
        }

        String token = tokenService.createToken(customer.getId().toString(), Role.CUSTOMER);

        log.info("Successfully login the customer with email {}", email);

        return  token;
    }

    @Override
    public String loginAdmin(String email, String password) {

        log.info("Attempting to login admin with email {}", email);

        AdminEntity adminEntity = adminRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        Admin admin = adminMapper.toDomain(adminEntity);

        boolean passwordMatches = passwordEncoder.matches(password, admin.getPassword());

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }
        if (admin.getStatus() == Status.BLOCKED) {
            throw new AccountIsBlockedException();
        }

        String token = tokenService.createToken(admin.getId().toString(), admin.getRole());

        log.info("Successfully login the admin with email {}", email);

        return token;
    }
}
