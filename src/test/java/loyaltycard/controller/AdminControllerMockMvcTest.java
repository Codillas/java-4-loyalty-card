package loyaltycard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import loyaltycard.controller.dto.AdminDto;
import loyaltycard.controller.dto.LoginRequestDto;
import loyaltycard.controller.dto.RoleDto;
import loyaltycard.controller.dto.SignUpRequestDto;
import loyaltycard.controller.dto.StatusDto;
import loyaltycard.exception.AdminNotFoundException;
import loyaltycard.exception.GlobalExceptionHandler;
import loyaltycard.exception.InvalidCredentialsException;
import loyaltycard.mapper.AdminMapper;
import loyaltycard.service.AdminService;
import loyaltycard.service.AuthService;
import loyaltycard.service.model.Admin;
import loyaltycard.service.model.Role;
import loyaltycard.service.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMVC тести для AdminController.
 * Використовує standaloneSetup для тестування без Spring Context.
 *
 * Демонструє:
 *  - перевірку JSON-відповіді через jsonPath()
 *  - перевірку HTTP-статусів
 *  - тестування виняткових ситуацій через GlobalExceptionHandler
 *  - тестування публічних POST-запитів з тілом
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerMockMvcTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AdminService adminService;

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        // standaloneSetup — ручне налаштування MockMvc для одного контролера
        // setControllerAdvice — підключаємо GlobalExceptionHandler для обробки винятків
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------------------------------------------------------------------------
    // Допоміжні методи
    // ---------------------------------------------------------------------------

    private Admin buildAdmin(UUID id, String name, String email, Status status) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setName(name);
        admin.setEmail(email);
        admin.setPhoneNumber("+380991234567");
        admin.setPassword("encoded");
        admin.setRole(Role.ADMIN);
        admin.setStatus(status);
        admin.setCreatedAt(Instant.now());
        admin.setUpdatedAt(Instant.now());
        return admin;
    }

    private AdminDto buildAdminDto(UUID id, String name, String email, StatusDto statusDto) {
        AdminDto dto = new AdminDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        dto.setPhoneNumber("+380991234567");
        dto.setRoleDto(RoleDto.ADMIN);
        dto.setStatusDto(statusDto);
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        return dto;
    }

    // ===========================================================================
    // POST /admins (createAdmin)
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | POST /admins → 201 CREATED з даними нового адміна")
    void shouldReturn201WhenCreatingAdmin() throws Exception {

        // given
        UUID id = UUID.randomUUID();
        SignUpRequestDto requestDto = new SignUpRequestDto();
        requestDto.setName("New Admin");
        requestDto.setEmail("newadmin@test.com");
        requestDto.setPhoneNumber("+380991234567");
        requestDto.setPassword("Password1!");

        Admin admin = buildAdmin(id, "New Admin", "newadmin@test.com", Status.ACTIVE);
        AdminDto adminDto = buildAdminDto(id, "New Admin", "newadmin@test.com", StatusDto.ACTIVE);

        when(adminMapper.toDomain(any(SignUpRequestDto.class))).thenReturn(admin);
        when(adminService.createAdmin(admin)).thenReturn(admin);
        when(adminMapper.toDto(admin)).thenReturn(adminDto);

        // when & then
        // perform() → виконати HTTP запит
        // andExpect() → перевірити результат
        // jsonPath("$.поле") → перевірити значення поля в JSON
        mockMvc.perform(
                        post("/admins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))) // Серіалізуємо об'єкт у JSON
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("New Admin"))
                .andExpect(jsonPath("$.email").value("newadmin@test.com"))
                .andExpect(jsonPath("$.statusDto").value("ACTIVE"));

        verify(adminService).createAdmin(admin);
    }

    // ===========================================================================
    // POST /admins/login (публічний endpoint)
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | POST /admins/login → 200 OK з токеном")
    void shouldReturn200WithTokenWhenAdminLoginIsValid() throws Exception {

        // given
        LoginRequestDto loginRequest = new LoginRequestDto("admin@test.com", "Password1!");

        when(authService.loginAdmin("admin@test.com", "Password1!")).thenReturn("jwt-token-value");

        // when & then
        mockMvc.perform(
                        post("/admins/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-value"));
    }

    @Test
    @DisplayName("MockMVC | POST /admins/login → 403 при невірних кредентіалах")
    void shouldReturn403WhenAdminLoginCredentialsAreInvalid() throws Exception {

        LoginRequestDto loginRequest = new LoginRequestDto("wrong@test.com", "wrongPass");

        // thenThrow — налаштовуємо мок кинути виняток
        when(authService.loginAdmin("wrong@test.com", "wrongPass"))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(
                        post("/admins/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    // ===========================================================================
    // GET /admins
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | GET /admins → 200 OK зі списком адмінів")
    void shouldReturn200WithAdminListWhenGetAll() throws Exception {

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Admin a1 = buildAdmin(id1, "Admin One", "a1@test.com", Status.ACTIVE);
        Admin a2 = buildAdmin(id2, "Admin Two", "a2@test.com", Status.ACTIVE);
        AdminDto dto1 = buildAdminDto(id1, "Admin One", "a1@test.com", StatusDto.ACTIVE);
        AdminDto dto2 = buildAdminDto(id2, "Admin Two", "a2@test.com", StatusDto.ACTIVE);

        when(adminService.getAdmins()).thenReturn(List.of(a1, a2));
        when(adminMapper.toDto(a1)).thenReturn(dto1);
        when(adminMapper.toDto(a2)).thenReturn(dto2);

        // jsonPath("$") → кореневий елемент (масив)
        // jsonPath("$[0].name") → перший елемент масиву
        mockMvc.perform(get("/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))    // Перевіряємо розмір масиву
                .andExpect(jsonPath("$[0].name").value("Admin One"))
                .andExpect(jsonPath("$[1].name").value("Admin Two"));
    }

    // ===========================================================================
    // GET /admins/{adminId}
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | GET /admins/{adminId} → 200 OK з даними адміна")
    void shouldReturn200WhenGettingAdminById() throws Exception {

        UUID id = UUID.randomUUID();
        Admin admin = buildAdmin(id, "Test Admin", "test@test.com", Status.ACTIVE);
        AdminDto adminDto = buildAdminDto(id, "Test Admin", "test@test.com", StatusDto.ACTIVE);

        when(adminService.getAdminById(id)).thenReturn(admin);
        when(adminMapper.toDto(admin)).thenReturn(adminDto);

        mockMvc.perform(get("/admins/{adminId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Test Admin"));
    }

    @Test
    @DisplayName("MockMVC | GET /admins/{adminId} → 404 якщо адмін не існує")
    void shouldReturn404WhenAdminNotFound() throws Exception {

        UUID id = UUID.randomUUID();
        // GlobalExceptionHandler перетворює виняток у 404 відповідь
        when(adminService.getAdminById(id)).thenThrow(new AdminNotFoundException(id));

        mockMvc.perform(get("/admins/{adminId}", id))
                .andExpect(status().isNotFound());
    }

    // ===========================================================================
    // PUT /admins/{adminId}/activate
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | PUT /admins/{adminId}/activate → 200 OK зі статусом ACTIVE")
    void shouldReturn200WhenActivatingAdmin() throws Exception {

        UUID id = UUID.randomUUID();
        Admin admin = buildAdmin(id, "Admin", "a@test.com", Status.ACTIVE);
        AdminDto adminDto = buildAdminDto(id, "Admin", "a@test.com", StatusDto.ACTIVE);

        when(adminService.activateAdmin(id)).thenReturn(admin);
        when(adminMapper.toDto(admin)).thenReturn(adminDto);

        mockMvc.perform(put("/admins/{adminId}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusDto").value("ACTIVE"));
    }

    // ===========================================================================
    // PUT /admins/{adminId}/block
    // ===========================================================================

    @Test
    @DisplayName("MockMVC | PUT /admins/{adminId}/block → 200 OK зі статусом BLOCKED")
    void shouldReturn200WhenBlockingAdmin() throws Exception {

        UUID id = UUID.randomUUID();
        Admin admin = buildAdmin(id, "Admin", "a@test.com", Status.BLOCKED);
        AdminDto adminDto = buildAdminDto(id, "Admin", "a@test.com", StatusDto.BLOCKED);

        when(adminService.blockAdmin(id)).thenReturn(admin);
        when(adminMapper.toDto(admin)).thenReturn(adminDto);

        mockMvc.perform(put("/admins/{adminId}/block", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusDto").value("BLOCKED"));

        verify(adminService).blockAdmin(id);
    }

    @Test
    @DisplayName("MockMVC | PUT /admins/{adminId}/block → 404 якщо адміна не існує")
    void shouldReturn404WhenBlockingNonExistentAdmin() throws Exception {

        UUID id = UUID.randomUUID();
        when(adminService.blockAdmin(id)).thenThrow(new AdminNotFoundException(id));

        mockMvc.perform(put("/admins/{adminId}/block", id))
                .andExpect(status().isNotFound());
    }
}
