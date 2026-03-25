package com.waitless.queueservice.service.counter;

import com.waitless.queueservice.dto.CounterDTO;
import com.waitless.queueservice.entity.Counter;
import com.waitless.queueservice.entity.Queue;
import com.waitless.queueservice.exception.BusinessException;
import com.waitless.queueservice.mapper.CounterMapper;
import com.waitless.queueservice.repository.CounterRepository;
import com.waitless.queueservice.repository.QueueRepository;
import com.waitless.queueservice.service.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CounterServiceTest {

    @Mock private CounterRepository counterRepository;
    @Mock private QueueRepository queueRepository;
    @Mock private CounterMapper counterMapper;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private CounterServiceImpl counterService;

    private Counter counter;
    private CounterDTO counterDTO;
    private Queue queue;

    @BeforeEach
    void setUp() {
        queue = new Queue();
        queue.setId(1L);
        queue.setIsActive(true);

        counter = new Counter();
        counter.setId(1L);
        counter.setCounterNumber(1);
        counter.setQueue(queue);
        counter.setIsActive(false);

        counterDTO = new CounterDTO();
        counterDTO.setId(1L);
        counterDTO.setCounterNumber(1);
        counterDTO.setQueueId(1L);
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    @Test
    void createCounter_Success() {
        when(queueRepository.findById(1L)).thenReturn(Optional.of(queue));
        when(counterMapper.toEntity(any(CounterDTO.class))).thenReturn(counter);
        when(counterRepository.save(any(Counter.class))).thenReturn(counter);
        when(counterMapper.toDTO(any(Counter.class))).thenReturn(counterDTO);

        CounterDTO result = counterService.createCounter(counterDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(counter.getIsActive()).isFalse();
        verify(counterRepository).save(any(Counter.class));
    }

    @Test
    void createCounter_QueueNotFound_ThrowsRuntimeException() {
        when(queueRepository.findById(99L)).thenReturn(Optional.empty());
        counterDTO.setQueueId(99L);

        assertThrows(RuntimeException.class, () -> counterService.createCounter(counterDTO));
        verify(counterRepository, never()).save(any());
    }

    // ── GET BY QUEUE ───────────────────────────────────────────────────────────

    @Test
    void getCountersByQueue_ReturnsCounters() {
        when(counterRepository.findByQueueId(1L)).thenReturn(List.of(counter));
        when(counterMapper.toDTO(counter)).thenReturn(counterDTO);

        List<CounterDTO> result = counterService.getCountersByQueue(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQueueId()).isEqualTo(1L);
    }

    @Test
    void getCountersByQueue_EmptyQueue_ReturnsEmptyList() {
        when(counterRepository.findByQueueId(1L)).thenReturn(List.of());

        List<CounterDTO> result = counterService.getCountersByQueue(1L);

        assertThat(result).isEmpty();
    }

    // ── GET BY ID ──────────────────────────────────────────────────────────────

    @Test
    void getCounterById_Success() {
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));
        when(counterMapper.toDTO(counter)).thenReturn(counterDTO);

        CounterDTO result = counterService.getCounterById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getCounterById_NotFound_ThrowsRuntimeException() {
        when(counterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> counterService.getCounterById(99L));
    }

    // ── ACTIVE COUNT ───────────────────────────────────────────────────────────

    @Test
    void getActiveCountersCount_ReturnsCount() {
        when(counterRepository.countByQueueIdAndIsActive(1L, true)).thenReturn(3);

        Integer count = counterService.getActiveCountersCount(1L);

        assertThat(count).isEqualTo(3);
    }

    @Test
    void getActiveCountersCount_NoActiveCounters_ReturnsZero() {
        when(counterRepository.countByQueueIdAndIsActive(1L, true)).thenReturn(0);

        Integer count = counterService.getActiveCountersCount(1L);

        assertThat(count).isEqualTo(0);
    }

    // ── OPEN ───────────────────────────────────────────────────────────────────

    @Test
    void openCounter_Success() {
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));
        when(counterRepository.save(any(Counter.class))).thenReturn(counter);
        when(counterMapper.toDTO(any(Counter.class))).thenReturn(counterDTO);

        CounterDTO result = counterService.openCounter(1L);

        assertThat(result).isNotNull();
        assertThat(counter.getIsActive()).isTrue();
        verify(eventPublisher).publishCounterOpened(any(Counter.class));
        verify(counterRepository).save(counter);
    }

    @Test
    void openCounter_QueueInactive_ThrowsBusinessException() {
        queue.setIsActive(false);
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));

        assertThrows(BusinessException.class, () -> counterService.openCounter(1L));
        verify(counterRepository, never()).save(any());
    }

    @Test
    void openCounter_NotFound_ThrowsRuntimeException() {
        when(counterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> counterService.openCounter(99L));
    }

    // ── CLOSE ──────────────────────────────────────────────────────────────────

    @Test
    void closeCounter_Success() {
        counter.setIsActive(true);
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));
        when(counterRepository.save(any(Counter.class))).thenReturn(counter);
        when(counterMapper.toDTO(any(Counter.class))).thenReturn(counterDTO);

        CounterDTO result = counterService.closeCounter(1L);

        assertThat(result).isNotNull();
        assertThat(counter.getIsActive()).isFalse();
        verify(eventPublisher).publishCounterOpened(any(Counter.class));
        verify(counterRepository).save(counter);
    }

    @Test
    void closeCounter_QueueInactive_ThrowsBusinessException() {
        queue.setIsActive(false);
        counter.setIsActive(true);
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));

        assertThrows(BusinessException.class, () -> counterService.closeCounter(1L));
        verify(counterRepository, never()).save(any());
    }

    @Test
    void closeCounter_NotFound_ThrowsRuntimeException() {
        when(counterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> counterService.closeCounter(99L));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    @Test
    void deleteCounter_Success() {
        counter.setIsActive(false);
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));
        doNothing().when(counterRepository).deleteById(1L);

        counterService.deleteCounter(1L);

        verify(counterRepository).deleteById(1L);
    }

    @Test
    void deleteCounter_ActiveCounter_ThrowsBusinessException() {
        counter.setIsActive(true);
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));

        assertThrows(BusinessException.class, () -> counterService.deleteCounter(1L));
        verify(counterRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteCounter_NotFound_ThrowsException() {
        when(counterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> counterService.deleteCounter(99L));
    }
}
