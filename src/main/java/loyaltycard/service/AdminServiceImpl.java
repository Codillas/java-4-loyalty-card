package loyaltycard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import loyaltycard.exception.AdminAlreadyExistException;
import loyaltycard.exception.AdminNotFoundException;
import loyaltycard.mapper.AdminMapper;
import loyaltycard.repository.AdminRepository;
import loyaltycard.repository.entity.AdminEntity;
import loyaltycard.service.model.Admin;
import loyaltycard.service.model.Role;
import loyaltycard.service.model.Status;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;

    @Override
    public Admin createAdmin(Admin admin) {

        log.info("Attempting to create an admin with email {}", admin.getEmail());

        Optional<AdminEntity> optionalAdmin = adminRepository.findByEmail(admin.getEmail());

        if (optionalAdmin.isPresent()) {
            throw new AdminAlreadyExistException(admin.getEmail());
        }

        admin.setStatus(Status.ACTIVE);
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(Instant.now());
        admin.setUpdatedAt(Instant.now());

        AdminEntity adminEntity = adminMapper.toEntity(admin);

        AdminEntity savedAdmin = adminRepository.save(adminEntity);


        log.info("Succsessfully created an admin with email {}", admin.getEmail());

        return adminMapper.toDomain(savedAdmin);
    }

    @Override
    public List<Admin> getAdmins() {

        List<AdminEntity> adminEntityList = adminRepository.findAll();

        List<Admin> adminList = adminEntityList.stream().
                map(adminEntity -> adminMapper.toDomain(adminEntity))
                .toList();

        return adminList;
    }

    @Override
    public Admin getAdminById(UUID adminId) {

        Optional<AdminEntity> optionalAdmin = adminRepository.findById(adminId);

        if (optionalAdmin.isEmpty()) {
            throw new AdminNotFoundException(adminId);
        }

        AdminEntity adminEntity = optionalAdmin.get();
        Admin admin = adminMapper.toDomain(adminEntity);

        return admin;
    }


    @Override
    public Admin activateAdmin(UUID adminId) {


        Optional<AdminEntity> adminActive = adminRepository.findById(adminId);

        if (adminActive.isEmpty()) {
            throw new AdminNotFoundException(adminId);
        }

        AdminEntity adminEntity = adminActive.get();
        adminEntity.setStatus(loyaltycard.repository.entity.Status.ACTIVE);
        adminEntity.setUpdatedAt(Instant.now());
        AdminEntity savedAdmin = adminRepository.save(adminEntity);
        Admin admin = adminMapper.toDomain(savedAdmin);


        return admin;
    }


    @Override
    public Admin blockAdmin(UUID adminId) {

        Optional<AdminEntity> adminActive = adminRepository.findById(adminId);

        if (adminActive.isEmpty()) {
            throw new AdminNotFoundException(adminId);
        }

        AdminEntity adminEntity = adminActive.get();
        adminEntity.setStatus(loyaltycard.repository.entity.Status.BLOCKED);
        adminEntity.setUpdatedAt(Instant.now());
        AdminEntity savedAdmin = adminRepository.save(adminEntity);
        Admin admin = adminMapper.toDomain(savedAdmin);


        return admin;
    }

}
