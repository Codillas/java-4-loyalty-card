package loyaltycard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import loyaltycard.exception.AdminAlreadyExistException;
import loyaltycard.exception.AdminNotFoundException;
import loyaltycard.service.model.Admin;
import loyaltycard.service.model.Role;
import loyaltycard.service.model.Status;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private HashMap<UUID, Admin> adminHashMap = new HashMap<>();

    @Override
    public Admin createAdmin(Admin admin) {

        log.info("Attempting to create an admin with email {}", admin.getEmail());

        Optional<Admin> optionalAdmin = adminHashMap.values().stream()
                .filter(admin -> admin.getEmail().equals(admin.getEmail()))
                .findFirst();

        if (optionalAdmin.isPresent()) {
            throw new AdminAlreadyExistException(admin.getEmail());
        }

        admin.setId(UUID.randomUUID());
        admin.setStatus(Status.ACTIVE);
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(Instant.now());
        admin.setUpdatedAt(Instant.now());

        adminHashMap.put(admin.getId(), admin);

        log.info("Succsessfully created an admin with email {}", admin.getEmail());

        return admin;
    }

    @Override
    public List<Admin> getAdmins() {

        return adminHashMap.values().stream().toList();
    }

    @Override
    public Admin getAdminById(UUID adminId) {

        Admin admin = adminHashMap.get(adminId);

        if (admin != null) {
            return admin;
        } else {
            throw new AdminNotFoundException(adminId);
        }
    }

    @Override
    public Admin activateAdmin(UUID adminId) {

        Admin admin = adminHashMap.get(adminId);
        admin.setStatus(Status.ACTIVE);
        admin.setUpdatedAt(Instant.now());
        adminHashMap.put(admin.getId(), admin);

        return admin;
    }

    @Override
    public Admin blockAdmin(UUID adminId) {

        Admin admin = adminHashMap.get(adminId);
        admin.setStatus(Status.BLOCKED);
        admin.setUpdatedAt(Instant.now());
        adminHashMap.put(admin.getId(), admin);

        return admin;
    }
}
