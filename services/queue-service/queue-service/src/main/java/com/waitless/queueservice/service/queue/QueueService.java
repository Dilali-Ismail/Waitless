package com.waitless.queueservice.service.queue;


import com.waitless.queueservice.dto.QueueDTO;

import java.util.List;

public interface QueueService {
    public QueueDTO createQueue(QueueDTO queueDTO);
    public List<QueueDTO> getAllQueues();
    public QueueDTO getQueueById(Long id);
    public List<QueueDTO> getQueuesByCompany(Long companyId);
    public List<QueueDTO> getActiveQueues();
    public QueueDTO updateQueue(Long id, QueueDTO queueDTO);
    public QueueDTO openQueue(Long id);
    public QueueDTO closeQueue(Long id);
    public void deleteQueue(Long id);

}
