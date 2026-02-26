package com.waitless.queueservice.repository;

import com.waitless.queueservice.entity.Queue;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QueueRepository extends CrudRepository<Queue,Long> {

    List<Queue> findByCompanyId(Long companyId);

    List<Queue> findByCompanyIdAndIsActive(Long companyId, Boolean isActive);

    List<Queue> findByIsActive(Boolean isActive);

    @Query("SELECT q FROM Queue q WHERE q.company.id = :companyId AND q.isActive = true")
    List<Queue> findActiveQueuesByCompany(@Param("companyId") Long companyId);

}
