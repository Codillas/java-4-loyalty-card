package loyaltycard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import loyaltycard.controller.dto.CustomerDto;
import loyaltycard.controller.dto.UpdateCustomerRequestDto;
import loyaltycard.mapper.CustomerMapper;
import loyaltycard.service.CustomerService;
import loyaltycard.service.model.Customer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @Secured("ADMIN")
    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {

        List<Customer> customerList = customerService.getCustomers();
        List<CustomerDto> customerDtoList = customerList.stream().map(customerMapper::toDto).toList();

        return ResponseEntity.ok().body(customerDtoList);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN') or #customerId.toString() == authentication.name")
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable UUID customerId) {

        Customer customer = customerService.getCustomerById(customerId);
        CustomerDto customerDto = customerMapper.toDto(customer);

        return ResponseEntity.ok().body(customerDto);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN') or #customerId.toString() == authentication.name")
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomerRequestDto updateCustomerRequestDto) {

        Customer customer = customerMapper.toDomain(updateCustomerRequestDto);
        Customer updatedCustomer = customerService.updateCustomer(customerId, customer);
        CustomerDto customerDto = customerMapper.toDto(updatedCustomer);

        return ResponseEntity.ok().body(customerDto);
    }

    @Secured("ADMIN")
    @PutMapping("/{customerId}/activate")
    public ResponseEntity<CustomerDto> activateCustomer(@PathVariable UUID customerId) {

        Customer customer = customerService.activateCustomer(customerId);
        CustomerDto customerDto = customerMapper.toDto(customer);

        return ResponseEntity.ok().body(customerDto);
    }

    @Secured("ADMIN")
    @PutMapping("/{customerId}/block")
    public ResponseEntity<CustomerDto> blockCustomer(@PathVariable UUID customerId) {

        Customer customer = customerService.blockCustomer(customerId);
        CustomerDto customerDto = customerMapper.toDto(customer);

        return ResponseEntity.ok().body(customerDto);
    }
}
