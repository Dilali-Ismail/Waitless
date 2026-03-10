package com.waitless.user.repository;

import com.waitless.user.entity.UserRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserRestrictionRepository extends JpaRepository<UserRestriction,Integer> {

    List<UserRestriction> findByUserIdOrderByAppliedAtDesc(String userId);
    List<UserRestriction> findByUserIdAndRestrictionType(String userId, String restrictionType);
    List<UserRestriction> findByUserIdAndAppliedAtAfter(String userId, LocalDateTime date);
    long countByUserIdAndRestrictionType(String userId, String restrictionType);
}
