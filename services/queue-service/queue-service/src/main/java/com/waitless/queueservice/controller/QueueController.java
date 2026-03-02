package com.waitless.queueservice.controller;


import com.waitless.queueservice.dto.QueueDTO;
import com.waitless.queueservice.service.queue.QueueService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @PostMapping
    public ResponseEntity<QueueDTO> createQueue(@Valid @RequestBody QueueDTO queueDTO){
        QueueDTO created = queueService.createQueue(queueDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<QueueDTO>> getAllQueues(){
        List<QueueDTO> List = queueService.getAllQueues();
        return ResponseEntity.ok(List);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QueueDTO> getQueue(@PathVariable Long id){
        QueueDTO queue = queueService.getQueueById(id);
        return ResponseEntity.ok(queue);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<QueueDTO>> getAllQueuesByCompany(@PathVariable Long companyId){
        List<QueueDTO> ListByCompany = queueService.getQueuesByCompany(companyId);
        return ResponseEntity.ok(ListByCompany);
    }

    @GetMapping("/active")
    public ResponseEntity<List<QueueDTO>> getAllActiveQueues(){
        List<QueueDTO> ListActive = queueService.getActiveQueues();
        return ResponseEntity.ok(ListActive);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QueueDTO> updateQueue(
            @PathVariable Long id,
           @Valid @RequestBody QueueDTO queueDTO){

        QueueDTO updated = queueService.updateQueue(id, queueDTO);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/open")
    public ResponseEntity<QueueDTO> openQueue(@PathVariable Long id){
        QueueDTO open = queueService.openQueue(id);
        return ResponseEntity.ok(open);
    }
    @PutMapping("/{id}/close")
    public ResponseEntity<QueueDTO> closeQueue(@PathVariable Long id){
        QueueDTO close = queueService.closeQueue(id);
        return ResponseEntity.ok(close);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletQueue(@PathVariable Long id){
        queueService.deleteQueue(id);
        return ResponseEntity.noContent().build();
    }






}
