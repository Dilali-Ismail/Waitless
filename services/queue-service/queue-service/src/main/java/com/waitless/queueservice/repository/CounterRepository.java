package com.waitless.queueservice.repository;

import com.waitless.queueservice.entity.Counter;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CounterRepository extends CrudRepository<Counter, Long> {

    List<Counter> findByCompanyId(Long companyId);

    List<Counter> findByQueueIdAndIsActive(Long queueId, Boolean isActive);

    Integer countByQueueIdAndIsActive(Long queueId, Boolean isActive);

}
