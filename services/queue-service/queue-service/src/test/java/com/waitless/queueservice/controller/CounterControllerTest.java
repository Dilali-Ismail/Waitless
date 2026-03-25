package com.waitless.queueservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitless.queueservice.dto.CounterDTO;
import com.waitless.queueservice.exception.BusinessException;
import com.waitless.queueservice.exception.ResourceNotFoundException;
import com.waitless.queueservice.service.counter.CounterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import com.waitless.queueservice.config.TestSecurityConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CounterController.class)
@Import(TestSecurityConfig.class)
class CounterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CounterService counterService;

    @MockBean
    private io.micrometer.tracing.Tracer tracer;

    private CounterDTO counterDTO;

    @BeforeEach
    void setUp() {
        counterDTO = CounterDTO.builder()
                .id(1L)
                .counterNumber(1)
                .queueId(1L)
                .isActive(false)
                .build();
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCounter_Success_Returns201() throws Exception {
        when(counterService.createCounter(any(CounterDTO.class))).thenReturn(counterDTO);

        mockMvc.perform(post("/api/counters")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(counterDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.counterNumber").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCounter_QueueNotFound_Returns500() throws Exception {
        when(counterService.createCounter(any(CounterDTO.class)))
                .thenThrow(new RuntimeException("queue not found"));

        mockMvc.perform(post("/api/counters")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(counterDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void createCounter_Forbidden_Returns403() throws Exception {
        mockMvc.perform(post("/api/counters")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(counterDTO)))
                .andExpect(status().isForbidden());
    }

    // ── GET BY ID ──────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCounterById_Success_Returns200() throws Exception {
        when(counterService.getCounterById(1L)).thenReturn(counterDTO);

        mockMvc.perform(get("/api/counters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCounterById_NotFound_Returns500() throws Exception {
        when(counterService.getCounterById(99L))
                .thenThrow(new RuntimeException("counter not found"));

        mockMvc.perform(get("/api/counters/99"))
                .andExpect(status().isInternalServerError());
    }

    // ── GET BY QUEUE ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCountersByQueue_Returns200() throws Exception {
        when(counterService.getCountersByQueue(1L)).thenReturn(List.of(counterDTO));

        mockMvc.perform(get("/api/counters/queue/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].queueId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCountersByQueue_Empty_Returns200() throws Exception {
        when(counterService.getCountersByQueue(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/counters/queue/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── ACTIVE COUNT ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getActiveCountersCount_Returns200() throws Exception {
        when(counterService.getActiveCountersCount(1L)).thenReturn(3);

        mockMvc.perform(get("/api/counters/queue/1/active/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));
    }

    // ── OPEN ───────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void openCounter_Success_Returns200() throws Exception {
        CounterDTO opened = CounterDTO.builder().id(1L).counterNumber(1).queueId(1L).isActive(true).build();
        when(counterService.openCounter(1L)).thenReturn(opened);

        mockMvc.perform(patch("/api/counters/1/open").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void openCounter_QueueInactive_Returns400() throws Exception {
        when(counterService.openCounter(1L))
                .thenThrow(new BusinessException("La file d'attente doit être active"));

        mockMvc.perform(patch("/api/counters/1/open").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ── CLOSE ──────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void closeCounter_Success_Returns200() throws Exception {
        CounterDTO closed = CounterDTO.builder().id(1L).counterNumber(1).queueId(1L).isActive(false).build();
        when(counterService.closeCounter(1L)).thenReturn(closed);

        mockMvc.perform(patch("/api/counters/1/close").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCounter_Success_Returns204() throws Exception {
        doNothing().when(counterService).deleteCounter(1L);

        mockMvc.perform(delete("/api/counters/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCounter_ActiveCounter_Returns400() throws Exception {
        doThrow(new BusinessException("Le guichet doit être fermé"))
                .when(counterService).deleteCounter(1L);

        mockMvc.perform(delete("/api/counters/1").with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
