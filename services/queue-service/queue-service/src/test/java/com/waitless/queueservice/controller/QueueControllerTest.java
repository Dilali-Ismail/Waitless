package com.waitless.queueservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitless.queueservice.config.TestSecurityConfig;
import com.waitless.queueservice.dto.QueueDTO;
import com.waitless.queueservice.exception.ResourceNotFoundException;
import com.waitless.queueservice.service.queue.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueueController.class)
@Import(TestSecurityConfig.class)
class QueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QueueService queueService;

    @MockBean
    private io.micrometer.tracing.Tracer tracer;

    private QueueDTO queueDTO;

    @BeforeEach
    void setUp() {
        queueDTO = QueueDTO.builder()
                .id(1L)
                .name("Main Queue")
                .capacity(50)
                .averageServiceTime(5)
                .companyId(1L)
                .isActive(true)
                .build();
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createQueue_Success_Returns201() throws Exception {
        when(queueService.createQueue(any(QueueDTO.class))).thenReturn(queueDTO);

        mockMvc.perform(post("/api/queues")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queueDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Main Queue"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createQueue_CompanyNotFound_Returns404() throws Exception {
        when(queueService.createQueue(any(QueueDTO.class)))
                .thenThrow(new ResourceNotFoundException("company not found"));

        mockMvc.perform(post("/api/queues")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queueDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void createQueue_Forbidden_Returns403() throws Exception {
        mockMvc.perform(post("/api/queues")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queueDTO)))
                .andExpect(status().isForbidden());
    }

    // ── GET ALL ────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllQueues_Returns200WithList() throws Exception {
        when(queueService.getAllQueues()).thenReturn(List.of(queueDTO));

        mockMvc.perform(get("/api/queues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Main Queue"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllQueues_Empty_Returns200EmptyList() throws Exception {
        when(queueService.getAllQueues()).thenReturn(List.of());

        mockMvc.perform(get("/api/queues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET BY ID ──────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQueueById_Success_Returns200() throws Exception {
        when(queueService.getQueueById(1L)).thenReturn(queueDTO);

        mockMvc.perform(get("/api/queues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQueueById_NotFound_Returns404() throws Exception {
        when(queueService.getQueueById(99L))
                .thenThrow(new ResourceNotFoundException("queue not found"));

        mockMvc.perform(get("/api/queues/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET BY COMPANY ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getQueuesByCompany_Returns200() throws Exception {
        when(queueService.getQueuesByCompany(1L)).thenReturn(List.of(queueDTO));

        mockMvc.perform(get("/api/queues/company/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyId").value(1L));
    }

    // ── GET ACTIVE ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getActiveQueues_Returns200() throws Exception {
        when(queueService.getActiveQueues()).thenReturn(List.of(queueDTO));

        mockMvc.perform(get("/api/queues/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQueue_Success_Returns200() throws Exception {
        QueueDTO updated = QueueDTO.builder()
                .id(1L).name("Updated Queue").capacity(100).averageServiceTime(10).companyId(1L).build();
        when(queueService.updateQueue(eq(1L), any(QueueDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/queues/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Queue"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateQueue_NotFound_Returns404() throws Exception {
        when(queueService.updateQueue(eq(99L), any(QueueDTO.class)))
                .thenThrow(new ResourceNotFoundException("queue not found"));

        mockMvc.perform(put("/api/queues/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queueDTO)))
                .andExpect(status().isNotFound());
    }

    // ── OPEN ───────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void openQueue_Success_Returns200() throws Exception {
        QueueDTO opened = QueueDTO.builder()
                .id(1L).name("Q").capacity(50).averageServiceTime(5).companyId(1L).isActive(true).build();
        when(queueService.openQueue(1L)).thenReturn(opened);

        mockMvc.perform(put("/api/queues/1/open").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void openQueue_NotFound_Returns404() throws Exception {
        when(queueService.openQueue(99L))
                .thenThrow(new ResourceNotFoundException("queue not found"));

        mockMvc.perform(put("/api/queues/99/open").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ── CLOSE ──────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void closeQueue_Success_Returns200() throws Exception {
        QueueDTO closed = QueueDTO.builder()
                .id(1L).name("Q").capacity(50).averageServiceTime(5).companyId(1L).isActive(false).build();
        when(queueService.closeQueue(1L)).thenReturn(closed);

        mockMvc.perform(put("/api/queues/1/close").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteQueue_Success_Returns204() throws Exception {
        doNothing().when(queueService).deleteQueue(1L);

        mockMvc.perform(delete("/api/queues/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteQueue_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("queue not found"))
                .when(queueService).deleteQueue(99L);

        mockMvc.perform(delete("/api/queues/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
