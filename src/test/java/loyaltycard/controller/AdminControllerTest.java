package loyaltycard.controller;

import loyaltycard.controller.dto.AdminDto;
import loyaltycard.controller.dto.LoginRequestDto;
import loyaltycard.controller.dto.SignUpRequestDto;
import loyaltycard.controller.dto.TokenResponseDto;
import loyaltycard.mapper.AdminMapper;
import loyaltycard.service.AdminService;
import loyaltycard.service.AuthService;
import loyaltycard.service.model.Admin;
import loyaltycard.service.model.Role;
import loyaltycard.service.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdminControllerTest {

    // --- Stub implementations ---

    private static class StubAdminService implements AdminService {

        final UUID fixedId = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");

        private Admin buildAdmin(String name, String email) {
            Admin admin = new Admin();
            admin.setId(fixedId);
            admin.setName(name);
            admin.setEmail(email);
            admin.setPhoneNumber("+380991234567");
            admin.setRole(Role.ADMIN);
            admin.setStatus(Status.ACTIVE);
            admin.setCreatedAt(Instant.now());
            admin.setUpdatedAt(Instant.now());
            return admin;
        }

        @Override
        public Admin createAdmin(Admin admin) {
            return buildAdmin(admin.getName(), admin.getEmail());
        }

        @Override
        public List<Admin> getAdmins() {
            return List.of(
                    buildAdmin("Admin One", "one@mail.com"),
                    buildAdmin("Admin Two", "two@mail.com")
            );
        }

        @Override
        public Admin getAdminById(UUID id) {
            return buildAdmin("Admin One", "one@mail.com");
        }

        @Override
        public Admin activateAdmin(UUID id) {
            Admin admin = buildAdmin("Admin One", "one@mail.com");
            admin.setStatus(Status.ACTIVE);
            return admin;
        }

        @Override
        public Admin blockAdmin(UUID id) {
            Admin admin = buildAdmin("Admin One", "one@mail.com");
            admin.setStatus(Status.BLOCKED);
            return admin;
        }
    }

    private static class StubAuthService implements AuthService {

        @Override
        public String signUp(loyaltycard.service.model.Customer customer) {
            return "customer-token";
        }

        @Override
        public String loginCustomer(String email, String password) {
            return "customer-token";
        }

        @Override
        public String loginAdmin(String email, String password) {
            return "admin-token";
        }
    }

    private AdminController adminController;
    private StubAdminService stubAdminService;

    @BeforeEach
    void setUp() {
        stubAdminService = new StubAdminService();
        AdminMapper adminMapper = new AdminMapper();
        adminController = new AdminController(stubAdminService, adminMapper, new StubAuthService());
    }

    // -----------------------------------------------------------------------
    // POST /admins (createAdmin)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("createAdmin: returns 201 CREATED with admin body")
    void shouldReturnCreatedWhenCreateAdminRequestIsValid() {

        //given
        SignUpRequestDto requestDto = new SignUpRequestDto();
        requestDto.setName("New Admin");
        requestDto.setEmail("newadmin@mail.com");
        requestDto.setPhoneNumber("+380991234560");
        requestDto.setPassword("AdminPass123!");

        //when
        ResponseEntity<AdminDto> response = adminController.createAdmin(requestDto);

        //then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Admin", response.getBody().getName());
    }

    @Test
    @DisplayName("createAdmin: returned admin has a non-null ID")
    void shouldReturnAdminWithNonNullIdWhenAdminIsCreated() {

        //given
        SignUpRequestDto requestDto = new SignUpRequestDto();
        requestDto.setName("Another Admin");
        requestDto.setEmail("another@mail.com");
        requestDto.setPhoneNumber("+380991234561");
        requestDto.setPassword("AdminPass123!");

        //when
        ResponseEntity<AdminDto> response = adminController.createAdmin(requestDto);

        //then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
    }

    // -----------------------------------------------------------------------
    // POST /admins/login
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("adminLogin: returns 200 OK with a token")
    void shouldReturnOkWhenAdminLoginCredentialsAreValid() {

        //given
        LoginRequestDto loginRequest = new LoginRequestDto("admin@mail.com", "AdminPass123!");

        //when
        ResponseEntity<TokenResponseDto> response = adminController.login(loginRequest);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("admin-token", response.getBody().getToken());
    }

    // -----------------------------------------------------------------------
    // GET /admins
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getAllAdmins: returns 200 OK with a non-empty list")
    void shouldReturnOkWithListWhenGetAllAdmins() {

        //given — stub always returns 2 admins

        //when
        ResponseEntity<List<AdminDto>> response = adminController.getAllAdmins();

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    // -----------------------------------------------------------------------
    // GET /admins/{adminId}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getAdminById: returns 200 OK with correct admin")
    void shouldReturnOkWithAdminWhenAdminIdIsValid() {

        //given
        UUID adminId = stubAdminService.fixedId;

        //when
        ResponseEntity<AdminDto> response = adminController.getAdminById(adminId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(adminId, response.getBody().getId());
    }

    // -----------------------------------------------------------------------
    // PUT /admins/{adminId}/activate
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("activateAdmin: returns 200 OK and admin status is ACTIVE")
    void shouldReturnActiveStatusWhenAdminIsActivated() {

        //given
        UUID adminId = stubAdminService.fixedId;

        //when
        ResponseEntity<AdminDto> response = adminController.activateAdmin(adminId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(loyaltycard.controller.dto.StatusDto.ACTIVE, response.getBody().getStatusDto());
    }

    // -----------------------------------------------------------------------
    // PUT /admins/{adminId}/block
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("blockAdmin: returns 200 OK and admin status is BLOCKED")
    void shouldReturnBlockedStatusWhenAdminIsBlocked() {

        //given
        UUID adminId = stubAdminService.fixedId;

        //when
        ResponseEntity<AdminDto> response = adminController.blockAdmin(adminId);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(loyaltycard.controller.dto.StatusDto.BLOCKED, response.getBody().getStatusDto());
    }
}
