package com.waitless.user.controller;


import com.waitless.user.dto.CreateUserRequest;
import com.waitless.user.dto.UpdateUserRequest;
import com.waitless.user.dto.UserDTO;
import com.waitless.user.enums.UserStatus;
import com.waitless.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {

        UserDTO createdUser = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserByUserId(@PathVariable String userId) {

        UserDTO user = userService.getUserByUserId(userId);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating user: userId={}", userId);

        UserDTO updatedUser = userService.updateUser(userId, request);

        log.info("User updated successfully: userId={}", userId);

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {

        List<UserDTO> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserDTO>> getUsersByStatus(@PathVariable UserStatus status) {

        List<UserDTO> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.debug("Fetching user by email: {}", email);

        UserDTO user = userService.getUserByEmail(email);

        return ResponseEntity.ok(user);
    }

}
