package loyaltycard.service;

import loyaltycard.exception.AdminAlreadyExistException;
import loyaltycard.exception.AdminNotFoundException;
import loyaltycard.mapper.AdminMapper;
import loyaltycard.repository.AdminRepository;
import loyaltycard.repository.entity.AdminEntity;
import loyaltycard.repository.entity.RoleEntity;
import loyaltycard.repository.entity.StatusEntity;
import loyaltycard.service.model.Admin;
import loyaltycard.service.model.Role;
import loyaltycard.service.model.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit-тести для AdminServiceImpl з Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminServiceImpl adminService;

    // ---------------------------------------------------------------------------
    // Допоміжні методи
    // ---------------------------------------------------------------------------

    private AdminEntity buildAdminEntity(UUID id, String email, StatusEntity status) {
        AdminEntity entity = new AdminEntity();
        entity.setId(id);
        entity.setName("Test Admin");
        entity.setEmail(email);
        entity.setPhoneNumber("+380991234567");
        entity.setPassword("encoded_password");
        entity.setRoleEntity(RoleEntity.ADMIN);
        entity.setStatusEntity(status);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private Admin buildAdmin(UUID id, String email, Status status) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setName("Test Admin");
        admin.setEmail(email);
        admin.setPhoneNumber("+380991234567");
        admin.setPassword("encoded_password");
        admin.setRole(Role.ADMIN);
        admin.setStatus(status);
        admin.setCreatedAt(Instant.now());
        admin.setUpdatedAt(Instant.now());
        return admin;
    }

    // ===========================================================================
    // createAdmin()
    // ===========================================================================

    @Test
    @DisplayName("createAdmin: успішно створює адміна, якщо email ще не зайнятий")
    void shouldCreateAdminSuccessfully() {

        // given
        UUID id = UUID.randomUUID();
        String email = "admin@test.com";

        Admin inputAdmin = new Admin();
        inputAdmin.setName("New Admin");
        inputAdmin.setEmail(email);
        inputAdmin.setPassword("plainPassword");

        AdminEntity savedEntity = buildAdminEntity(id, email, StatusEntity.ACTIVE);
        Admin expectedAdmin = buildAdmin(id, email, Status.ACTIVE);

        // Мок: email ще не зайнятий
        when(adminRepository.findByEmail(email)).thenReturn(Optional.empty());
        // Мок: кодування пароля
        when(passwordEncoder.encode("plainPassword")).thenReturn("encoded_password");
        when(adminMapper.toEntity(inputAdmin)).thenReturn(savedEntity);
        when(adminRepository.save(savedEntity)).thenReturn(savedEntity);
        when(adminMapper.toDomain(savedEntity)).thenReturn(expectedAdmin);

        // when
        Admin result = adminService.createAdmin(inputAdmin);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(Status.ACTIVE, result.getStatus());
        assertEquals(Role.ADMIN, result.getRole());

        // Перевіряємо, що пароль було закодовано
        verify(passwordEncoder).encode("plainPassword");
        verify(adminRepository).save(savedEntity);
    }

    @Test
    @DisplayName("createAdmin: кидає AdminAlreadyExistException, якщо email вже зайнятий")
    void shouldThrowAdminAlreadyExistExceptionWhenEmailIsTaken() {

        // given
        String email = "duplicate@test.com";
        Admin inputAdmin = new Admin();
        inputAdmin.setEmail(email);

        AdminEntity existingEntity = buildAdminEntity(UUID.randomUUID(), email, StatusEntity.ACTIVE);
        when(adminRepository.findByEmail(email)).thenReturn(Optional.of(existingEntity));

        // then
        assertThrows(AdminAlreadyExistException.class, () -> adminService.createAdmin(inputAdmin));
        verify(adminRepository, never()).save(any());
    }

    // ===========================================================================
    // getAdmins()
    // ===========================================================================

    @Test
    @DisplayName("getAdmins: повертає список усіх адмінів")
    void shouldReturnAllAdmins() {

        // given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        AdminEntity e1 = buildAdminEntity(id1, "a1@test.com", StatusEntity.ACTIVE);
        AdminEntity e2 = buildAdminEntity(id2, "a2@test.com", StatusEntity.ACTIVE);
        Admin a1 = buildAdmin(id1, "a1@test.com", Status.ACTIVE);
        Admin a2 = buildAdmin(id2, "a2@test.com", Status.ACTIVE);

        when(adminRepository.findAll()).thenReturn(List.of(e1, e2));
        when(adminMapper.toDomain(e1)).thenReturn(a1);
        when(adminMapper.toDomain(e2)).thenReturn(a2);

        // when
        List<Admin> result = adminService.getAdmins();

        // then
        assertEquals(2, result.size());
        verify(adminRepository).findAll();
    }

    // ===========================================================================
    // getAdminById()
    // ===========================================================================

    @Test
    @DisplayName("getAdminById: повертає адміна, якщо він існує")
    void shouldReturnAdminById() {

        UUID id = UUID.randomUUID();
        AdminEntity entity = buildAdminEntity(id, "admin@test.com", StatusEntity.ACTIVE);
        Admin expectedAdmin = buildAdmin(id, "admin@test.com", Status.ACTIVE);

        when(adminRepository.findById(id)).thenReturn(Optional.of(entity));
        when(adminMapper.toDomain(entity)).thenReturn(expectedAdmin);

        Admin result = adminService.getAdminById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("getAdminById: кидає AdminNotFoundException, якщо адміна не існує")
    void shouldThrowAdminNotFoundExceptionWhenAdminDoesNotExist() {

        UUID id = UUID.randomUUID();
        when(adminRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(AdminNotFoundException.class, () -> adminService.getAdminById(id));
    }

    // ===========================================================================
    // activateAdmin()
    // ===========================================================================

    @Test
    @DisplayName("activateAdmin: встановлює статус ACTIVE і зберігає адміна")
    void shouldActivateAdminSuccessfully() {

        UUID id = UUID.randomUUID();
        AdminEntity entity = buildAdminEntity(id, "admin@test.com", StatusEntity.BLOCKED);
        AdminEntity saved = buildAdminEntity(id, "admin@test.com", StatusEntity.ACTIVE);
        Admin expectedAdmin = buildAdmin(id, "admin@test.com", Status.ACTIVE);

        when(adminRepository.findById(id)).thenReturn(Optional.of(entity));
        when(adminRepository.save(entity)).thenReturn(saved);
        when(adminMapper.toDomain(saved)).thenReturn(expectedAdmin);

        Admin result = adminService.activateAdmin(id);

        assertEquals(Status.ACTIVE, result.getStatus());
        verify(adminRepository).save(entity);
        assertEquals(StatusEntity.ACTIVE, entity.getStatusEntity());
    }

    @Test
    @DisplayName("activateAdmin: кидає AdminNotFoundException, якщо адміна не існує")
    void shouldThrowWhenActivatingNonExistentAdmin() {

        UUID id = UUID.randomUUID();
        when(adminRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(AdminNotFoundException.class, () -> adminService.activateAdmin(id));
    }

    // ===========================================================================
    // blockAdmin()
    // ===========================================================================

    @Test
    @DisplayName("blockAdmin: встановлює статус BLOCKED і зберігає адміна")
    void shouldBlockAdminSuccessfully() {

        UUID id = UUID.randomUUID();
        AdminEntity entity = buildAdminEntity(id, "admin@test.com", StatusEntity.ACTIVE);
        AdminEntity saved = buildAdminEntity(id, "admin@test.com", StatusEntity.BLOCKED);
        Admin expectedAdmin = buildAdmin(id, "admin@test.com", Status.BLOCKED);

        when(adminRepository.findById(id)).thenReturn(Optional.of(entity));
        when(adminRepository.save(entity)).thenReturn(saved);
        when(adminMapper.toDomain(saved)).thenReturn(expectedAdmin);

        Admin result = adminService.blockAdmin(id);

        assertEquals(Status.BLOCKED, result.getStatus());
        verify(adminRepository).save(entity);
        assertEquals(StatusEntity.BLOCKED, entity.getStatusEntity());
    }

    @Test
    @DisplayName("blockAdmin: кидає AdminNotFoundException, якщо адміна не існує")
    void shouldThrowWhenBlockingNonExistentAdmin() {

        UUID id = UUID.randomUUID();
        when(adminRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(AdminNotFoundException.class, () -> adminService.blockAdmin(id));
    }
}
