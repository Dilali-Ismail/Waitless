package com.waitless.user.service;

import com.waitless.user.entity.User;
import com.waitless.user.enums.UserStatus;
import com.waitless.user.exception.UserNotFoundException;
import com.waitless.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoringServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private ScoringService scoringService;

    // ─── addPoints ────────────────────────────────────────────────────────────

    @Test
    void addPoints_ShouldIncreaseScore_WhenUserExists() {
        User user = User.builder().userId("user1").score(100.0).build();

        when(userRepository.findByUserId("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        scoringService.addPoints("user1", 30.0, "ticket completed");

        assertEquals(130.0, user.getScore());
        verify(userRepository).save(user);
    }

    @Test
    void addPoints_ShouldCapAt200_WhenScoreExceedsMax() {
        User user = User.builder().userId("user1").score(190.0).build();

        when(userRepository.findByUserId("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        scoringService.addPoints("user1", 50.0, "bonus");

        assertEquals(200.0, user.getScore());
    }

    @Test
    void addPoints_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByUserId("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> scoringService.addPoints("ghost", 10.0, "reason"));
        verify(userRepository, never()).save(any());
    }

    // ─── deductPoints ─────────────────────────────────────────────────────────

    @Test
    void deductPoints_ShouldDecreaseScore_WhenUserExists() {
        User user = User.builder().userId("user1").score(100.0).build();

        when(userRepository.findByUserId("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        scoringService.deductPoints("user1", 20.0, "warning");

        assertEquals(80.0, user.getScore());
        verify(userRepository).save(user);
    }

    @Test
    void deductPoints_ShouldFloorAt0_WhenScoreGoesNegative() {
        User user = User.builder().userId("user1").score(10.0).build();

        when(userRepository.findByUserId("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        scoringService.deductPoints("user1", 50.0, "no-show");

        assertEquals(0.0, user.getScore());
    }

    @Test
    void deductPoints_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByUserId("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> scoringService.deductPoints("ghost", 10.0, "reason"));
        verify(userRepository, never()).save(any());
    }
}
