package loyaltycard.controller;

import loyaltycard.controller.dto.AdminDto;
import loyaltycard.controller.dto.Role;
import loyaltycard.controller.dto.SignUpRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
public class AdminController {

    private HashMap<UUID, AdminDto> adminDtoHashMap = new HashMap<>();

    @PostMapping("/admins")
    public ResponseEntity<AdminDto> createAdmin(@RequestBody SignUpRequestDto signUpRequestDto) {

        AdminDto adminDto = new AdminDto();
        adminDto.setId(UUID.randomUUID());
        adminDto.setFirstName(signUpRequestDto.getFirstName());
        adminDto.setLastName(signUpRequestDto.getLastName());
        adminDto.setEmail(signUpRequestDto.getEmail());
        adminDto.setPhoneNumber(signUpRequestDto.getPhoneNumber());
        adminDto.setRole(Role.ADMIN);

        Instant now = Instant.now();

        adminDto.setCreatedAt(now);
        adminDto.setUpdatedAt(now);

        adminDtoHashMap.put(adminDto.getId(), adminDto);

     return ResponseEntity.ok().body(adminDto);
    }

    @GetMapping("/admins")
    public ResponseEntity<List<AdminDto>> getAllAdmins() {

        List<AdminDto> adminDtoList = adminDtoHashMap.values().stream().toList();

        return ResponseEntity.ok().body(adminDtoList);
    }

}



