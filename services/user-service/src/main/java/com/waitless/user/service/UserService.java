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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    public UserDTO createUser(CreateUserRequest request){

        if (userRepository.existsByUserId(request.getUserId())) {
            throw new UserAlreadyExistsException(
                    "User with userId " + request.getUserId() + " already exists"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User with email " + request.getEmail() + " already exists"
            );
        }

        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);


    }

    public UserDTO getUserByUserId(String userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    return new UserNotFoundException("User not found: " + userId);
                });

        return userMapper.toDTO(user);
    }

    public UserDTO getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    return new UserNotFoundException("User not found: " + email);
                });

        return userMapper.toDTO(user);
    }

    public UserDTO updateUser(String userId, UpdateUserRequest request) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    return new UserNotFoundException("User not found: " + userId);
                });

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {

                throw new UserAlreadyExistsException(
                        "Email " + request.getEmail() + " is already in use"
                );
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);

        return userMapper.toDTO(updatedUser);
    }

    public List<UserDTO> getAllUsers() {

        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteUser(String userId) {
        log.info("Deleting user with userId: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("User not found with userId: {}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });

        userRepository.delete(user);

        log.info("User deleted successfully: userId={}", userId);
    }


}
