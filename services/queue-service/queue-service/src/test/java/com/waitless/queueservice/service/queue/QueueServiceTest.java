package com.waitless.queueservice.service.queue;

import com.waitless.queueservice.dto.QueueDTO;
import com.waitless.queueservice.entity.Company;
import com.waitless.queueservice.entity.Queue;
import com.waitless.queueservice.enums.CompanyStatus;
import com.waitless.queueservice.exception.ResourceNotFoundException;
import com.waitless.queueservice.mapper.QueueMapper;
import com.waitless.queueservice.repository.CompanyRepository;
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
public class QueueServiceTest {

    @Mock private QueueRepository queueRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private QueueMapper queueMapper;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private QueueServiceImpl queueService;

    private Queue queue;
    private QueueDTO queueDTO;
    private Company company;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);
        company.setStatus(CompanyStatus.ACTIVE);

        queue = new Queue();
        queue.setId(1L);
        queue.setName("Test Queue");
        queue.setCompany(company);
        queue.setIsActive(true);

        queueDTO = new QueueDTO();
        queueDTO.setId(1L);
        queueDTO.setName("Test Queue");
        queueDTO.setCompanyId(1L);
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    @Test
    void createQueue_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(queueMapper.toEntity(any(QueueDTO.class))).thenReturn(queue);
        when(queueRepository.save(any(Queue.class))).thenReturn(queue);
        when(queueMapper.toDTO(any(Queue.class))).thenReturn(queueDTO);

        QueueDTO result = queueService.createQueue(queueDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(queue.getIsActive()).isTrue();
        verify(eventPublisher).publishQueueCreated(any(Queue.class));
        verify(queueRepository).save(any(Queue.class));
    }

    @Test
    void createQueue_CompanyNotFound_ThrowsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());
        queueDTO.setCompanyId(99L);

        assertThrows(ResourceNotFoundException.class, () -> queueService.createQueue(queueDTO));
        verify(queueRepository, never()).save(any());
    }

    @Test
    void createQueue_CompanyNotActive_ThrowsResourceNotFoundException() {
        company.setStatus(CompanyStatus.SUSPENDED);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        assertThrows(ResourceNotFoundException.class, () -> queueService.createQueue(queueDTO));
        verify(queueRepository, never()).save(any());
    }

    // ── GET ALL ────────────────────────────────────────────────────────────────

    @Test
    void getAllQueues_ReturnsAll() {
        when(queueRepository.findAll()).thenReturn(List.of(queue));
        when(queueMapper.toDTO(queue)).thenReturn(queueDTO);

        List<QueueDTO> result = queueService.getAllQueues();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Queue");
    }

    @Test
    void getAllQueues_EmptyList_ReturnsEmpty() {
        when(queueRepository.findAll()).thenReturn(List.of());

        List<QueueDTO> result = queueService.getAllQueues();

        assertThat(result).isEmpty();
    }

    // ── GET BY ID ──────────────────────────────────────────────────────────────

    @Test
    void getQueueById_Success() {
        when(queueRepository.findById(1L)).thenReturn(Optional.of(queue));
        when(queueMapper.toDTO(any(Queue.class))).thenReturn(queueDTO);

        QueueDTO result = queueService.getQueueById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getQueueById_NotFound_ThrowsResourceNotFoundException() {
        when(queueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> queueService.getQueueById(99L));
    }

    // ── GET BY COMPANY ─────────────────────────────────────────────────────────

    @Test
    void getQueuesByCompany_ReturnsFiltered() {
        when(queueRepository.findByCompanyId(1L)).thenReturn(List.of(queue));
        when(queueMapper.toDTO(queue)).thenReturn(queueDTO);

        List<QueueDTO> result = queueService.getQueuesByCompany(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getQueuesByCompany_NoneFound_ReturnsEmpty() {
        when(queueRepository.findByCompanyId(99L)).thenReturn(List.of());

        List<QueueDTO> result = queueService.getQueuesByCompany(99L);

        assertThat(result).isEmpty();
    }

    // ── GET ACTIVE ─────────────────────────────────────────────────────────────

    @Test
    void getActiveQueues_ReturnsActive() {
        when(queueRepository.findByIsActive(true)).thenReturn(List.of(queue));
        when(queueMapper.toDTO(queue)).thenReturn(queueDTO);

        List<QueueDTO> result = queueService.getActiveQueues();

        assertThat(result).hasSize(1);
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    @Test
    void updateQueue_Success() {
        QueueDTO updateDTO = new QueueDTO();
        updateDTO.setName("Updated Queue");

        when(queueRepository.findById(1L)).thenReturn(Optional.of(queue));
        when(queueRepository.save(any(Queue.class))).thenReturn(queue);
        when(queueMapper.toDTO(any(Queue.class))).thenReturn(queueDTO);

        QueueDTO result = queueService.updateQueue(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(queueMapper).updateEntityFromDTO(updateDTO, queue);
        verify(queueRepository).save(queue);
    }

    @Test
    void updateQueue_NotFound_ThrowsResourceNotFoundException() {
        when(queueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> queueService.updateQueue(99L, queueDTO));
        verify(queueRepository, never()).save(any());
    }

    // ── OPEN ───────────────────────────────────────────────────────────────────

    @Test
    void openQueue_Success() {
        queue.setIsActive(false);
        when(queueRepository.findById(1L)).thenReturn(Optional.of(queue));
        when(queueRepository.save(any(Queue.class))).thenReturn(queue);
        when(queueMapper.toDTO(any(Queue.class))).thenReturn(queueDTO);

        QueueDTO result = queueService.openQueue(1L);

        assertThat(result).isNotNull();
        assertThat(queue.getIsActive()).isTrue();
        verify(eventPublisher).publishQueueOpened(any(Queue.class));
        verify(queueRepository).save(queue);
    }

    @Test
    void openQueue_NotFound_ThrowsResourceNotFoundException() {
        when(queueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> queueService.openQueue(99L));
    }

    // ── CLOSE ──────────────────────────────────────────────────────────────────

    @Test
    void closeQueue_Success() {
        when(queueRepository.findById(1L)).thenReturn(Optional.of(queue));
        when(queueRepository.save(any(Queue.class))).thenReturn(queue);
        when(queueMapper.toDTO(any(Queue.class))).thenReturn(queueDTO);

        QueueDTO result = queueService.closeQueue(1L);

        assertThat(result).isNotNull();
        assertThat(queue.getIsActive()).isFalse();
        verify(eventPublisher).publishQueueClosed(any(Queue.class));
        verify(queueRepository).save(queue);
    }

    @Test
    void closeQueue_NotFound_ThrowsResourceNotFoundException() {
        when(queueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> queueService.closeQueue(99L));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    @Test
    void deleteQueue_Success() {
        when(queueRepository.existsById(1L)).thenReturn(true);
        doNothing().when(queueRepository).deleteById(1L);

        queueService.deleteQueue(1L);

        verify(queueRepository).deleteById(1L);
    }

    @Test
    void deleteQueue_NotFound_ThrowsResourceNotFoundException() {
        when(queueRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> queueService.deleteQueue(99L));
        verify(queueRepository, never()).deleteById(anyLong());
    }
}
