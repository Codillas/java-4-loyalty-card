package loyaltycard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import loyaltycard.controller.dto.AdminDto;
import loyaltycard.controller.dto.SignUpRequestDto;
import loyaltycard.mapper.AdminMapper;
import loyaltycard.service.AdminService;
import loyaltycard.service.model.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {


    private final AdminService adminService;
    private final AdminMapper adminMapper;


    @PostMapping
    public ResponseEntity<AdminDto> createAdmin(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {

        Admin admin = adminMapper.toDomain(signUpRequestDto);
        Admin createdAdmin = adminService.createAdmin(admin);
        AdminDto adminDto = adminMapper.toDto(createdAdmin);

     return ResponseEntity.ok().body(adminDto);
    }

    @GetMapping
    public ResponseEntity<List<AdminDto>> getAllAdmins() {

        List<Admin> adminList = adminService.getAdmins();

        List<AdminDto> adminDtoList = adminList.stream().map(adminMapper::toDto).toList();

        return ResponseEntity.ok().body(adminDtoList);
    }

    @GetMapping("/{adminId}")
    public ResponseEntity<AdminDto> getAdminById(@PathVariable UUID adminId) {

        Admin admin = adminService.getAdminById(adminId);

        AdminDto adminDto = adminMapper.toDto(admin);

        return ResponseEntity.ok().body(adminDto);

    }

    @PutMapping("/{adminId}/activate")
    public ResponseEntity<AdminDto> activateAdmin(@PathVariable UUID adminId) {

        Admin admin = adminService.activateAdmin(adminId);

        AdminDto adminDto = adminMapper.toDto(admin);

        return ResponseEntity.ok().body(adminDto);

    }

    @PutMapping("/{adminId}/block")
    public ResponseEntity<AdminDto> blockAdmin(@PathVariable UUID adminId) {

        Admin admin = adminService.blockAdmin(adminId);

        AdminDto adminDto = adminMapper.toDto(admin);

        return ResponseEntity.ok().body(adminDto);
    }

}
