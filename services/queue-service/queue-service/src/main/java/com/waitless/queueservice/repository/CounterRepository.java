package com.waitless.queueservice.repository;

import com.waitless.queueservice.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Arrays;
import java.util.List;

public interface CounterRepository extends JpaRepository<Counter, Long> {

    List<Counter> findByQueueId(Long queueId);

    List<Counter> findByQueueIdAndIsActive(Long queueId, Boolean isActive);

    Integer countByQueueIdAndIsActive(Long queueId, Boolean isActive);

}
