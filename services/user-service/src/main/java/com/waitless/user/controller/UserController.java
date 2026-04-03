package com.waitless.user.controller;


import com.waitless.user.dto.CreateUserRequest;
import com.waitless.user.dto.RegisterAgentRequest;
import com.waitless.user.dto.RegisterClientRequest;
import com.waitless.user.dto.UpdateUserRequest;
import com.waitless.user.dto.UserDTO;
import com.waitless.user.enums.UserStatus;
import com.waitless.user.service.KeycloakProvisioningService;
import com.waitless.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final KeycloakProvisioningService keycloakProvisioningService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {

        UserDTO createdUser = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }


    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerClient(@Valid @RequestBody RegisterClientRequest request) {

        String keycloakUserId = keycloakProvisioningService.createClientUser(request);

        CreateUserRequest dbRequest = CreateUserRequest.builder()
                .userId(keycloakUserId)
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();

        UserDTO created = userService.createUser(dbRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/register-agent")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<UserDTO> registerAgent(@Valid @RequestBody RegisterAgentRequest request) {
        String keycloakUserId = keycloakProvisioningService.createAgentUser(
                request.getName(), request.getEmail(), request.getPassword());

        CreateUserRequest dbRequest = CreateUserRequest.builder()
                .userId(keycloakUserId)
                .name(request.getName())
                .email(request.getEmail())
                .companyId(request.getCompanyId())
                .build();

        UserDTO created = userService.createUser(dbRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<UserDTO> getUserByUserId(@PathVariable("userId") String userId) {

        UserDTO user = userService.getUserByUserId(userId);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable("userId") String userId,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating user: userId={}", userId);

        UserDTO updatedUser = userService.updateUser(userId, request);

        log.info("User updated successfully: userId={}", userId);

        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> activateUser(@PathVariable("userId") String userId) {
        log.info("Activating user: userId={}", userId);
        UserDTO updatedUser = userService.activateUser(userId);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId) {

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {

        List<UserDTO> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<List<UserDTO>> getUsersByStatus(@PathVariable("status") UserStatus status) {

        List<UserDTO> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable("email") String email) {
        log.debug("Fetching user by email: {}", email);

        UserDTO user = userService.getUserByEmail(email);

        return ResponseEntity.ok(user);
    }

}
