package com.waitless.queueservice.repository;

import com.waitless.queueservice.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company,Long> {

    Optional<Company> findByEmail(String email);

    List<Company> findByStatus(String status);

    List<Company> findByCategory(String category);

    boolean existsByEmail(String email);
}
