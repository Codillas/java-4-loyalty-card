package loyaltycard.mapper;

import loyaltycard.controller.dto.AdminDto;
import loyaltycard.controller.dto.SignUpRequestDto;
import loyaltycard.service.model.Admin;
import loyaltycard.service.model.Role;
import loyaltycard.service.model.Status;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component
public class AdminMapper {

    public static Admin toDomain(AdminDto adminDto) {

        Admin admin = new Admin();
        admin.setId(adminDto.getId());
        admin.setFirstName(adminDto.getFirstName());
        admin.setLastName(adminDto.getLastName());
        admin.setEmail(adminDto.getEmail());
        admin.setRole(Role.valueOf(admin.getRole().name()));
        admin.setStatus(Status.valueOf(adminDto.getStatus().name()));
        admin.setPhoneNumber(adminDto.getPhoneNumber());
        admin.setCreatedAt(Instant.now());
        admin.setUpdatedAt(Instant.now());

        return admin;
    }

    public AdminDto toDto(Admin admin) {

        AdminDto adminDto = new AdminDto();
        adminDto.setId(admin.getId());
        adminDto.setFirstName(admin.getFirstName());
        adminDto.setLastName(admin.getLastName());
        adminDto.setEmail(admin.getEmail());
        adminDto.setPhoneNumber(admin.getPhoneNumber());
        adminDto.setRole(loyaltycard.controller.dto.Role.valueOf(admin.getRole().name()));
        adminDto.setStatus(loyaltycard.controller.dto.Status.valueOf(admin.getStatus().name()));
        adminDto.setCreatedAt(admin.getCreatedAt());
        adminDto.setUpdatedAt(admin.getUpdatedAt());

        return adminDto;
    }

    public Admin toDomain(SignUpRequestDto dto) {
        Admin admin = new Admin();
        admin.setFirstName(dto.getFirstName());
        admin.setLastName(dto.getLastName());
        admin.setEmail(dto.getEmail());
        admin.setPhoneNumber(dto.getPhoneNumber());
        admin.setPassword(dto.getPassword());

        return admin;
    }
}
