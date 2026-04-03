package com.waitless.queueservice.controller;


import com.waitless.queueservice.dto.QueueDTO;
import com.waitless.queueservice.service.queue.QueueService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<QueueDTO> createQueue(@Validated(QueueDTO.OnCreate.class) @RequestBody QueueDTO queueDTO){
        QueueDTO created = queueService.createQueue(queueDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<List<QueueDTO>> getAllQueues(){
        List<QueueDTO> List = queueService.getAllQueues();
        return ResponseEntity.ok(List);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<QueueDTO> getQueue(@PathVariable Long id){
        QueueDTO queue = queueService.getQueueById(id);
        return ResponseEntity.ok(queue);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<List<QueueDTO>> getAllQueuesByCompany(@PathVariable Long companyId){
        List<QueueDTO> ListByCompany = queueService.getQueuesByCompany(companyId);
        return ResponseEntity.ok(ListByCompany);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<List<QueueDTO>> getAllActiveQueues(){
        List<QueueDTO> ListActive = queueService.getActiveQueues();
        return ResponseEntity.ok(ListActive);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<QueueDTO> updateQueue(
            @PathVariable Long id,
           @Validated(QueueDTO.OnUpdate.class) @RequestBody QueueDTO queueDTO){

        QueueDTO updated = queueService.updateQueue(id, queueDTO);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<QueueDTO> openQueue(@PathVariable Long id){
        QueueDTO open = queueService.openQueue(id);
        return ResponseEntity.ok(open);
    }
    @PutMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<QueueDTO> closeQueue(@PathVariable Long id){
        QueueDTO close = queueService.closeQueue(id);
        return ResponseEntity.ok(close);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<Void> deletQueue(@PathVariable Long id){
        queueService.deleteQueue(id);
        return ResponseEntity.noContent().build();
    }






}
