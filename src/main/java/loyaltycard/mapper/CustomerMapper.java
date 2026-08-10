package loyaltycard.mapper;

import loyaltycard.controller.dto.CustomerDto;
import loyaltycard.controller.dto.SignUpRequestDto;
import loyaltycard.controller.dto.StatusDto;
import loyaltycard.controller.dto.UpdateCustomerRequestDto;
import loyaltycard.repository.entity.CustomerEntity;
import loyaltycard.repository.entity.StatusEntity;
import loyaltycard.service.model.Customer;
import loyaltycard.service.model.Status;
import org.springframework.stereotype.Component;


@Component
public class CustomerMapper {

    public Customer toDomain(SignUpRequestDto dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setPassword(dto.getPassword());

        return customer;
    }

    public Customer toDomain(CustomerEntity customerEntity) {

        Customer customer = new Customer();
        customer.setId(customerEntity.getId());
        customer.setName(customerEntity.getName());
        customer.setEmail(customerEntity.getEmail());
        customer.setPhoneNumber(customerEntity.getPhoneNumber());
        customer.setPassword(customerEntity.getPassword());
        customer.setStatus(Status.valueOf(customerEntity.getStatusEntity().name()));
        customer.setCreatedAt(customerEntity.getCreatedAt());
        customer.setUpdatedAt(customerEntity.getUpdatedAt());

        return customer;
    }

    public Customer toDomain(UpdateCustomerRequestDto dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());

        return customer;
    }

    public CustomerDto toDto(Customer customer) {

        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(customer.getId());
        customerDto.setName(customer.getName());
        customerDto.setEmail(customer.getEmail());
        customerDto.setPhoneNumber(customer.getPhoneNumber());
        customerDto.setStatusDto(StatusDto.valueOf(customer.getStatus().name()));
        customerDto.setCreatedAt(customer.getCreatedAt());
        customerDto.setUpdatedAt(customer.getUpdatedAt());

        return customerDto;
    }

    public CustomerEntity toEntity(Customer customer) {

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId(customer.getId());
        customerEntity.setName(customer.getName());
        customerEntity.setEmail(customer.getEmail());
        customerEntity.setPassword(customer.getPassword());
        customerEntity.setPhoneNumber(customer.getPhoneNumber());
        customerEntity.setStatusEntity(StatusEntity.valueOf(customer.getStatus().name()));
        customerEntity.setCreatedAt(customer.getCreatedAt());
        customerEntity.setUpdatedAt(customer.getUpdatedAt());

        return customerEntity;
    }

}
