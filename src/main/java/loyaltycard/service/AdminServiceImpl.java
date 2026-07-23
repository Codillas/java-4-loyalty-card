package loyaltycard.service;

import lombok.RequiredArgsConstructor;
import loyaltycard.service.model.Admin;
import loyaltycard.service.model.Role;
import loyaltycard.service.model.Status;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private HashMap<UUID, Admin> adminHashMap = new HashMap<>();

    @Override
    public Admin createAdmin(Admin admin) {

      admin.setId(UUID.randomUUID());
      admin.setStatus(Status.ACTIVE);
      admin.setRole(Role.ADMIN);
      admin.setCreatedAt(Instant.now());
      admin.setUpdatedAt(Instant.now());

      adminHashMap.put(admin.getId(), admin);

        return admin;
    }

    @Override
    public List<Admin> getAdmins() {

        return adminHashMap.values().stream().toList();
    }

    @Override
    public Admin getAdminById(UUID adminId) {

        return  adminHashMap.get(adminId);
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
