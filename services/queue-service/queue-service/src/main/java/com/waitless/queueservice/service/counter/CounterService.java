package com.waitless.queueservice.service.counter;

import com.waitless.queueservice.dto.CounterDTO;

import java.util.List;

public interface CounterService {

    public CounterDTO createCounter(CounterDTO counterDTO);
    public List<CounterDTO> getCountersByQueue(Long queueId);
    public CounterDTO getCounterById(Long id);
    public Integer getActiveCountersCount(Long queueId);
    public CounterDTO openCounter(Long id);
    public CounterDTO closeCounter(Long id);
    public void deleteCounter(Long id);
}
