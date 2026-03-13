package com.waitless.user.service;


import com.waitless.user.entity.User;
import com.waitless.user.exception.UserNotFoundException;
import com.waitless.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScoringService {

    private final UserRepository userRepository;

    private static final double MIN_SCORE = 0.0;
    private static final double MAX_SCORE = 200.0;

    public void addPoints(String userId, double points, String reason){

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        double newScore = user.getScore() + points;

        if (newScore > MAX_SCORE) {
            newScore = MAX_SCORE;
        }

        user.setScore(newScore);
        userRepository.save(user);
    }

    public void deductPoints(String userId, double points, String reason) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        double newScore = user.getScore() - points;

        if (newScore < MIN_SCORE) {
            newScore = MIN_SCORE;
        }

        user.setScore(newScore);
        userRepository.save(user);
    }


}
