package loyaltycard.mapper;

import loyaltycard.controller.dto.AdminDto;
import loyaltycard.controller.dto.RoleDto;
import loyaltycard.controller.dto.SignUpRequestDto;
import loyaltycard.controller.dto.StatusDto;
import loyaltycard.repository.entity.AdminEntity;
import loyaltycard.repository.entity.RoleEntity;
import loyaltycard.repository.entity.StatusEntity;
import loyaltycard.service.model.Admin;
import loyaltycard.service.model.Role;
import loyaltycard.service.model.Status;
import org.springframework.stereotype.Component;


@Component
public class AdminMapper {

    public Admin toDomain(AdminEntity adminEntity) {

        Admin admin = new Admin();
        admin.setId(adminEntity.getId());
        admin.setName(adminEntity.getName());
        admin.setEmail(adminEntity.getEmail());
        admin.setRole(Role.valueOf(adminEntity.getRoleEntity().name()));
        admin.setStatus(Status.valueOf(adminEntity.getStatusEntity().name()));
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
        adminDto.setRoleDto(RoleDto.valueOf(admin.getRole().name()));
        adminDto.setStatusDto(StatusDto.valueOf(admin.getStatus().name()));
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
        adminEntity.setRoleEntity(RoleEntity.valueOf(admin.getRole().name()));
        adminEntity.setStatusEntity(StatusEntity.valueOf(admin.getStatus().name()));
        adminEntity.setCreatedAt(admin.getCreatedAt());
        adminEntity.setUpdatedAt(admin.getUpdatedAt());

        return adminEntity;
    }

}
