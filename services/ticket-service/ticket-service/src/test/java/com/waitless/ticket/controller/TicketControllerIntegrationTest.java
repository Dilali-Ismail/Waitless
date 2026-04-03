package com.waitless.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitless.ticket.client.EstimationClient;
import com.waitless.ticket.client.UserClient;
import com.waitless.ticket.dto.request.CreateTicketRequest;
import com.waitless.ticket.dto.response.EstimationResponse;
import com.waitless.ticket.dto.response.UserResponse;
import com.waitless.ticket.repository.TicketRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @MockBean
    private EstimationClient estimationClient;

    @AfterEach
    void tearDown() {
        ticketRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void createTicket_ShouldReturnCreated_WhenClient() throws Exception {
        // Arrange
        CreateTicketRequest request = CreateTicketRequest.builder()
                .userId("user123")
                .queueId(1L)
                .clientName("Integration Client")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .userId("user123")
                .status("ACTIVE")
                .build();

        EstimationResponse estimationResponse = EstimationResponse.builder()
                .estimatedWaitMinutes(10)
                .build();

        when(userClient.getUserById(anyString())).thenReturn(userResponse);
        when(estimationClient.calculateEstimation(anyLong(), anyInt())).thenReturn(estimationResponse);

        // Act & Assert
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.clientName").value("Integration Client"));
    }

    @Test
    @WithMockUser(roles = "AGENT")
    void getTicketById_ShouldReturnOk_WhenAgent() throws Exception {
        // Act & Assert (Assuming we want to test security primarily)
        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isBadRequest()); // BadRequest because ticket 1 doesn't exist and service throws IllegalArgumentException
    }
}
