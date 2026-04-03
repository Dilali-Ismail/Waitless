package com.waitless.user.repository;

import com.waitless.user.entity.User;
import com.waitless.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String email);
    List<User> findByStatus(UserStatus status);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);


}
