package loyaltycard.mapper;

import loyaltycard.controller.dto.AdminDto;
import loyaltycard.controller.dto.SignUpRequestDto;
import loyaltycard.repository.entity.AdminEntity;
import loyaltycard.service.model.Admin;
import loyaltycard.service.model.Role;
import loyaltycard.service.model.Status;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component
public class AdminMapper {

    public Admin toDomain(AdminEntity adminEntity) {

        Admin admin = new Admin();
        admin.setId(adminEntity.getId());
        admin.setName(adminEntity.getName());
        admin.setEmail(adminEntity.getEmail());
        admin.setRole(Role.valueOf(adminEntity.getRole().name()));
        admin.setStatus(Status.valueOf(adminEntity.getStatus().name()));
        admin.setPhoneNumber(adminEntity.getPhoneNumber());
        admin.setCreatedAt(adminEntity.getCreatedAt());
        admin.setUpdatedAt(adminEntity.getUpdatedAt());

        return admin;
    }

    public Admin toDomain(SignUpRequestDto dto) {
        Admin admin = new Admin();
        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());
        admin.setPhoneNumber(dto.getPhoneNumber());
        admin.setPassword(dto.getPassword());

        return admin;
    }



    public AdminDto toDto(Admin admin) {

        AdminDto adminDto = new AdminDto();
        adminDto.setId(admin.getId());
        adminDto.setName(admin.getName());
        adminDto.setEmail(admin.getEmail());
        adminDto.setPhoneNumber(admin.getPhoneNumber());
        adminDto.setRole(loyaltycard.controller.dto.Role.valueOf(admin.getRole().name()));
        adminDto.setStatus(loyaltycard.controller.dto.Status.valueOf(admin.getStatus().name()));
        adminDto.setCreatedAt(admin.getCreatedAt());
        adminDto.setUpdatedAt(admin.getUpdatedAt());

        return adminDto;
    }

    public AdminEntity toEntity(Admin admin) {

        AdminEntity adminEntity = new AdminEntity();
        adminEntity.setId(admin.getId());
        adminEntity.setName(admin.getName());
        adminEntity.setEmail(admin.getEmail());
        adminEntity.setPassword(admin.getPassword());
        adminEntity.setPhoneNumber(admin.getPhoneNumber());
        adminEntity.setRole(loyaltycard.repository.entity.Role.valueOf(admin.getRole().name()));
        adminEntity.setStatus(loyaltycard.repository.entity.Status.valueOf(admin.getStatus().name()));
        adminEntity.setCreatedAt(admin.getCreatedAt());
        adminEntity.setUpdatedAt(admin.getUpdatedAt());

        return adminEntity;
    }

}
