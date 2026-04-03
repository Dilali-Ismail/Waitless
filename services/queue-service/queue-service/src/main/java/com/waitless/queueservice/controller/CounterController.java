package com.waitless.queueservice.controller;

import com.waitless.queueservice.dto.CounterDTO;
import com.waitless.queueservice.service.counter.CounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counters")
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<CounterDTO> createCounter(
            @Valid @RequestBody CounterDTO counterDTO) {

        CounterDTO created = counterService.createCounter(counterDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<CounterDTO> getCounterById(@PathVariable Long id) {

        CounterDTO counter = counterService.getCounterById(id);
        return ResponseEntity.ok(counter);
    }

    @GetMapping("/queue/{queueId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<List<CounterDTO>> getCountersByQueue(@PathVariable Long queueId) {
        List<CounterDTO> counters = counterService.getCountersByQueue(queueId);
        return ResponseEntity.ok(counters);
    }

    @GetMapping("/queue/{queueId}/active/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<Integer> getActiveCountersCount(@PathVariable Long queueId) {
        Integer count = counterService.getActiveCountersCount(queueId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<CounterDTO> openCounter(@PathVariable Long id) {
        CounterDTO opened = counterService.openCounter(id);
        return ResponseEntity.ok(opened);
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<CounterDTO> closeCounter(@PathVariable Long id) {
        CounterDTO closed = counterService.closeCounter(id);
        return ResponseEntity.ok(closed);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<Void> deleteCounter(@PathVariable Long id) {
        counterService.deleteCounter(id);
        return ResponseEntity.noContent().build();
    }

}
