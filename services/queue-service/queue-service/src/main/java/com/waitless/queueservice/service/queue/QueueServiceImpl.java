package com.waitless.queueservice.service.queue;


import com.waitless.queueservice.dto.QueueDTO;
import com.waitless.queueservice.entity.Company;
import com.waitless.queueservice.entity.Queue;
import com.waitless.queueservice.enums.CompanyStatus;
import com.waitless.queueservice.exception.RessourceNotFoundException;
import com.waitless.queueservice.mapper.QueueMapper;
import com.waitless.queueservice.repository.CompanyRepository;
import com.waitless.queueservice.repository.QueueRepository;
import com.waitless.queueservice.service.EventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    public final QueueRepository queueRepository;
    public final CompanyRepository companyRepository;
    public final QueueMapper queueMapper;
    public final EventPublisher eventPublisher;

    public QueueDTO createQueue(QueueDTO queueDTO) {
        Company company = companyRepository.
                findById(queueDTO.getCompanyId()).orElseThrow(() -> new RessourceNotFoundException("company not found"));

        if (!company.getStatus().equals(CompanyStatus.ACTIVE)) {
            throw new RessourceNotFoundException("company not active");
        }
        Queue queue = queueMapper.toEntity(queueDTO);
        queue.setCompany(company);
        queue.setIsActive(true);
        Queue saved = queueRepository.save(queue);
        eventPublisher.publishQueueCreated(saved);
        return queueMapper.toDTO(saved);

    }
    public List<QueueDTO> getAllQueues(){
        return queueRepository.findAll()
                .stream()
                .map(queueMapper::toDTO)
                .collect(Collectors.toList());
    }
    public QueueDTO getQueueById(Long id){

        Queue queue  = queueRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("queue not found"));
        return queueMapper.toDTO(queue);
    }

    public List<QueueDTO> getQueuesByCompany(Long companyId){

        return queueRepository.findByCompanyId(companyId)
                .stream()
                .map(queueMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<QueueDTO> getActiveQueues() {
        return queueRepository.findByIsActive(true)
                .stream()
                .map(queueMapper::toDTO)
                .collect(Collectors.toList());
    }

    public QueueDTO updateQueue(Long id, QueueDTO queueDTO) {

        Queue queue = queueRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("File d'attente introuvable avec l'ID: " + id));

        queueMapper.updateEntityFromDTO(queueDTO, queue);

        Queue updated = queueRepository.save(queue);

        return queueMapper.toDTO(updated);
    }

    public QueueDTO openQueue(Long id){
        Queue queue = queueRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("queue not found"));
        queue.setIsActive(true);
        Queue saved = queueRepository.save(queue);
        eventPublisher.publishQueueOpened(saved);
        return queueMapper.toDTO(saved);
    }

    public QueueDTO closeQueue(Long id){
        Queue queue = queueRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("queue not found"));
        queue.setIsActive(false);
        Queue saved = queueRepository.save(queue);
        eventPublisher.publishQueueClosed(saved);
        return queueMapper.toDTO(saved);
    }

    public void deleteQueue(Long id){

        if(!queueRepository.existsById(id)){
            throw new RessourceNotFoundException("queue not found");
        }
        queueRepository.deleteById(id);
    }


}
