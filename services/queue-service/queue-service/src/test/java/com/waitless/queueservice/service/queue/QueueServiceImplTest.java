package com.waitless.queueservice.service.queue;

import com.waitless.queueservice.dto.QueueDTO;
import com.waitless.queueservice.entity.Company;
import com.waitless.queueservice.entity.Queue;
import com.waitless.queueservice.enums.CompanyStatus;
import com.waitless.queueservice.mapper.QueueMapper;
import com.waitless.queueservice.repository.CompanyRepository;
import com.waitless.queueservice.repository.QueueRepository;
import com.waitless.queueservice.service.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueServiceImplTest {

    @Mock
    private QueueRepository queueRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private QueueMapper queueMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private QueueServiceImpl queueService;

    @Test
    void createQueue_ShouldReturnQueueDTO_WhenCompanyIsActive() {
        // Arrange
        QueueDTO inputDto = QueueDTO.builder()
                .name("Main Queue")
                .companyId(1L)
                .build();

        Company company = Company.builder()
                .id(1L)
                .status(CompanyStatus.ACTIVE)
                .build();

        Queue queue = Queue.builder()
                .name("Main Queue")
                .company(company)
                .build();

        QueueDTO outputDto = QueueDTO.builder()
                .id(1L)
                .name("Main Queue")
                .build();

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(queueMapper.toEntity(inputDto)).thenReturn(queue);
        when(queueRepository.save(any(Queue.class))).thenReturn(queue);
        when(queueMapper.toDTO(queue)).thenReturn(outputDto);

        // Act
        QueueDTO result = queueService.createQueue(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals("Main Queue", result.getName());
        verify(queueRepository).save(any(Queue.class));
        verify(eventPublisher).publishQueueCreated(any(Queue.class));
    }

    @Test
    void openQueue_ShouldSetActiveAndPublishEvent() {
        // Arrange
        Long queueId = 1L;
        Queue queue = Queue.builder().id(queueId).isActive(false).build();
        Queue savedQueue = Queue.builder().id(queueId).isActive(true).build();
        QueueDTO outputDto = QueueDTO.builder().id(queueId).isActive(true).build();

        when(queueRepository.findById(queueId)).thenReturn(Optional.of(queue));
        when(queueRepository.save(queue)).thenReturn(savedQueue);
        when(queueMapper.toDTO(savedQueue)).thenReturn(outputDto);

        // Act
        QueueDTO result = queueService.openQueue(queueId);

        // Assert
        assertTrue(result.getIsActive());
        verify(eventPublisher).publishQueueOpened(any(Queue.class));
    }
}
