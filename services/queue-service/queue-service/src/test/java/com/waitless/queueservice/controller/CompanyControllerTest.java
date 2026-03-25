package com.waitless.queueservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitless.queueservice.config.TestSecurityConfig;
import com.waitless.queueservice.dto.CompanyDTO;
import com.waitless.queueservice.exception.BusinessException;
import com.waitless.queueservice.exception.ResourceNotFoundException;
import com.waitless.queueservice.service.company.CompanyService;
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

@WebMvcTest(CompanyController.class)
@Import(TestSecurityConfig.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private io.micrometer.tracing.Tracer tracer;

    private CompanyDTO companyDTO;

    @BeforeEach
    void setUp() {
        companyDTO = CompanyDTO.builder()
                .id(1L)
                .name("Test Company")
                .email("test@company.com")
                .category("Restaurant")
                .build();
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCompany_Success_Returns201() throws Exception {
        when(companyService.createCompany(any(CompanyDTO.class))).thenReturn(companyDTO);

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@company.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCompany_DuplicateEmail_Returns400() throws Exception {
        when(companyService.createCompany(any(CompanyDTO.class)))
                .thenThrow(new BusinessException("Email already exists"));

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void createCompany_Forbidden_Returns403() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDTO)))
                .andExpect(status().isForbidden());
    }

    // ── GET ALL ────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCompanies_Returns200WithList() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of(companyDTO));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Company"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCompanies_Empty_Returns200EmptyList() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(List.of());

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET BY ID ──────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCompanyById_Success_Returns200() throws Exception {
        when(companyService.getCompanyById(1L)).thenReturn(companyDTO);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCompanyById_NotFound_Returns404() throws Exception {
        when(companyService.getCompanyById(99L))
                .thenThrow(new ResourceNotFoundException("Company not found"));

        mockMvc.perform(get("/api/companies/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET BY STATUS ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCompaniesByStatus_Returns200() throws Exception {
        when(companyService.getCompaniesByStatus("ACTIVE")).thenReturn(List.of(companyDTO));

        mockMvc.perform(get("/api/companies/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    // ── GET BY CATEGORY ────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCompaniesByCategory_Returns200() throws Exception {
        when(companyService.getCompaniesByCategory("Restaurant")).thenReturn(List.of(companyDTO));

        mockMvc.perform(get("/api/companies/category/Restaurant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Restaurant"));
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCompany_Success_Returns200() throws Exception {
        CompanyDTO updated = CompanyDTO.builder()
                .id(1L).name("Updated").email("updated@company.com").category("Bank").build();
        when(companyService.updateCompany(eq(1L), any(CompanyDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/companies/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCompany_NotFound_Returns404() throws Exception {
        when(companyService.updateCompany(eq(99L), any(CompanyDTO.class)))
                .thenThrow(new ResourceNotFoundException("Company not found"));

        mockMvc.perform(put("/api/companies/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyDTO)))
                .andExpect(status().isNotFound());
    }

    // ── ACTIVATE ───────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCompany_Success_Returns200() throws Exception {
        CompanyDTO activated = CompanyDTO.builder()
                .id(1L).name("Test").email("t@t.com").category("A").status("ACTIVE").build();
        when(companyService.activateCompany(1L)).thenReturn(activated);

        mockMvc.perform(put("/api/companies/1/activate").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCompany_NotFound_Returns404() throws Exception {
        when(companyService.activateCompany(99L))
                .thenThrow(new ResourceNotFoundException("Company not found"));

        mockMvc.perform(put("/api/companies/99/activate").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ── SUSPEND ────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void suspendCompany_Success_Returns200() throws Exception {
        CompanyDTO suspended = CompanyDTO.builder()
                .id(1L).name("Test").email("t@t.com").category("A").status("SUSPENDED").build();
        when(companyService.suspendCompany(1L)).thenReturn(suspended);

        mockMvc.perform(put("/api/companies/1/suspend").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCompany_Success_Returns204() throws Exception {
        doNothing().when(companyService).deleteCompany(1L);

        mockMvc.perform(delete("/api/companies/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCompany_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Company not found"))
                .when(companyService).deleteCompany(99L);

        mockMvc.perform(delete("/api/companies/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
