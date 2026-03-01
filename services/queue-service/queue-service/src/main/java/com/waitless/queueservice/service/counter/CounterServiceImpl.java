package com.waitless.queueservice.service.counter;

import com.waitless.queueservice.dto.CounterDTO;
import com.waitless.queueservice.entity.Counter;
import com.waitless.queueservice.entity.Queue;
import com.waitless.queueservice.exception.BusinessException;
import com.waitless.queueservice.mapper.CounterMapper;
import com.waitless.queueservice.repository.CounterRepository;
import com.waitless.queueservice.repository.QueueRepository;
import com.waitless.queueservice.service.EventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CounterServiceImpl implements CounterService {

    private final CounterRepository counterRepository;
    private final QueueRepository queueRepository;
    private final CounterMapper counterMapper;
    private final EventPublisher eventPublisher;

    public CounterDTO createCounter(CounterDTO counterDTO){

        Queue queue = queueRepository.findById(counterDTO.getQueueId())
                .orElseThrow(() -> new RuntimeException("queue not found")  );

        Counter counter = counterMapper.toEntity(counterDTO);
        counter.setQueue(queue);
        counter.setIsActive(false);

        Counter counterCreated = counterRepository.save(counter);

        return  counterMapper.toDTO(counterCreated);
    }

    public List<CounterDTO> getCountersByQueue(Long queueId){
        return counterRepository.findByQueueId(queueId)
                .stream()
                .map(counterMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CounterDTO getCounterById(Long id){

        Counter counter  = counterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("counter not found"));

        return  counterMapper.toDTO(counter);
    }

    public Integer getActiveCountersCount(Long queueId){

        return counterRepository.countByQueueIdAndIsActive(queueId,true);
    }

    public CounterDTO openCounter(Long id){

        Counter counter  = counterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("counter not found"));

        if(!counter.getQueue().getIsActive()){
            throw new BusinessException("La file d'attente doit être active pour ouvrir un guichet");
        }

        counter.setIsActive(true);

       Counter updated = counterRepository.save(counter);

       eventPublisher.publishCounterOpened(updated);

       return counterMapper.toDTO(updated);

    }
    public CounterDTO closeCounter(Long id){
        Counter counter  = counterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("counter not found"));

        if(!counter.getQueue().getIsActive()){
            throw new BusinessException("La file d'attente doit etre fermer pour ouvrir un guichet");
        }

        counter.setIsActive(false);

        Counter updated = counterRepository.save(counter);

        eventPublisher.publishCounterOpened(updated);

        return counterMapper.toDTO(updated);
    }

    public void deleteCounter(Long id){
        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guichet introuvable avec l'ID: " + id));

        if (counter.getIsActive()) {
            throw new BusinessException("Le guichet doit être fermé avant d'être supprimé");
        }
        counterRepository.deleteById(id);
    }
}
