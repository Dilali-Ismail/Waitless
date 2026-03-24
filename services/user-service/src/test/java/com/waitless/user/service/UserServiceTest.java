package com.waitless.user.service;

import com.waitless.user.dto.CreateUserRequest;
import com.waitless.user.dto.UpdateUserRequest;
import com.waitless.user.dto.UserDTO;
import com.waitless.user.entity.User;
import com.waitless.user.enums.UserStatus;
import com.waitless.user.exception.UserAlreadyExistsException;
import com.waitless.user.exception.UserNotFoundException;
import com.waitless.user.mapper.UserMapper;
import com.waitless.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    // ─── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_ShouldReturnUserDTO_WhenUserDoesNotExist() {
        CreateUserRequest request = CreateUserRequest.builder()
                .userId("user123").name("John Doe").email("john@example.com").build();

        User user = User.builder().userId("user123").name("John Doe").email("john@example.com").build();
        UserDTO userDTO = UserDTO.builder().userId("user123").name("John Doe").email("john@example.com").build();

        when(userRepository.existsByUserId(request.getUserId())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenUserIdAlreadyExists() {
        CreateUserRequest request = CreateUserRequest.builder()
                .userId("user123").build();

        when(userRepository.existsByUserId("user123")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        CreateUserRequest request = CreateUserRequest.builder()
                .userId("newUser").email("taken@example.com").build();

        when(userRepository.existsByUserId("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    // ─── getUserByUserId ───────────────────────────────────────────────────────

    @Test
    void getUserByUserId_ShouldReturnUserDTO_WhenUserExists() {
        User user = User.builder().userId("user123").build();
        UserDTO userDTO = UserDTO.builder().userId("user123").build();

        when(userRepository.findByUserId("user123")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserByUserId("user123");

        assertNotNull(result);
        assertEquals("user123", result.getUserId());
    }

    @Test
    void getUserByUserId_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findByUserId("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByUserId("nonexistent"));
    }

    // ─── updateUser ────────────────────────────────────────────────────────────

    @Test
    void updateUser_ShouldUpdateName_WhenNameIsProvided() {
        User user = User.builder().userId("user123").name("Old Name").email("old@example.com").build();
        UserDTO updatedDTO = UserDTO.builder().userId("user123").name("New Name").build();
        UpdateUserRequest request = UpdateUserRequest.builder().name("New Name").build();

        when(userRepository.findByUserId("user123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(any(User.class))).thenReturn(updatedDTO);

        UserDTO result = userService.updateUser("user123", request);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByUserId("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser("ghost", UpdateUserRequest.builder().build()));
    }

    @Test
    void updateUser_ShouldThrow_WhenNewEmailAlreadyTaken() {
        User user = User.builder().userId("user123").email("original@example.com").build();
        UpdateUserRequest request = UpdateUserRequest.builder().email("taken@example.com").build();

        when(userRepository.findByUserId("user123")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.updateUser("user123", request));
    }

    // ─── deleteUser ────────────────────────────────────────────────────────────

    @Test
    void deleteUser_ShouldDelete_WhenUserExists() {
        User user = User.builder().userId("user123").build();

        when(userRepository.findByUserId("user123")).thenReturn(Optional.of(user));

        userService.deleteUser("user123");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByUserId("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("ghost"));
        verify(userRepository, never()).delete(any());
    }

    // ─── getAllUsers / getUsersByStatus ────────────────────────────────────────

    @Test
    void getAllUsers_ShouldReturnMappedList() {
        User u1 = User.builder().userId("u1").build();
        User u2 = User.builder().userId("u2").build();
        UserDTO d1 = UserDTO.builder().userId("u1").build();
        UserDTO d2 = UserDTO.builder().userId("u2").build();

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));
        when(userMapper.toDTO(u1)).thenReturn(d1);
        when(userMapper.toDTO(u2)).thenReturn(d2);

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    void getUsersByStatus_ShouldReturnFilteredList() {
        User user = User.builder().userId("u1").status(UserStatus.SUSPENDED).build();
        UserDTO dto = UserDTO.builder().userId("u1").build();

        when(userRepository.findByStatus(UserStatus.SUSPENDED)).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(dto);

        List<UserDTO> result = userService.getUsersByStatus(UserStatus.SUSPENDED);

        assertEquals(1, result.size());
    }
}
