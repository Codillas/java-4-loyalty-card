package loyaltycard.service;

import loyaltycard.service.model.Admin;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    Admin createAdmin(Admin admin);

    List<Admin> getAdmins();

    Admin getAdminById(UUID id);

    Admin activateAdmin(UUID id);

    Admin blockAdmin(UUID id);
}
