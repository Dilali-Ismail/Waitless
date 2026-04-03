package com.waitless.queueservice.service.company;

import com.waitless.queueservice.dto.CompanyDTO;
import com.waitless.queueservice.entity.Company;
import com.waitless.queueservice.enums.CompanyStatus;
import com.waitless.queueservice.exception.BusinessException;
import com.waitless.queueservice.exception.ResourceNotFoundException;
import com.waitless.queueservice.mapper.CompanyMapper;
import com.waitless.queueservice.repository.CompanyRepository;
import com.waitless.queueservice.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private CompanyMapper companyMapper;
    @Mock private KeycloakCompanyProvisioningService keycloakCompanyProvisioningService;
    @Mock private S3Service s3Service;
    @InjectMocks private CompanyServiceImpl companyService;

    private Company company;
    private CompanyDTO companyDTO;

    @BeforeEach
    void setUp() {
        lenient().when(s3Service.resolveLogoUrlForClient(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().doNothing().when(keycloakCompanyProvisioningService).createCompanyAdminAccount(any());

        company = new Company();
        company.setId(1L);
        company.setName("Test Company");
        company.setEmail("test@test.com");
        company.setStatus(CompanyStatus.ACTIVE);

        companyDTO = new CompanyDTO();
        companyDTO.setId(1L);
        companyDTO.setName("Test Company");
        companyDTO.setEmail("test@test.com");
        companyDTO.setPassword("secret12");
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    @Test
    void createCompany_Success() {
        when(companyRepository.existsByEmail(anyString())).thenReturn(false);
        when(companyMapper.toEntity(any(CompanyDTO.class))).thenReturn(company);
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(companyMapper.toDto(any(Company.class))).thenReturn(companyDTO);

        CompanyDTO result = companyService.createCompany(companyDTO);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void createCompany_EmailExists_ThrowsBusinessException() {
        when(companyRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> companyService.createCompany(companyDTO));
        verify(companyRepository, never()).save(any());
    }

    // ── GET BY ID ──────────────────────────────────────────────────────────────

    @Test
    void getCompanyById_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyMapper.toDto(any(Company.class))).thenReturn(companyDTO);

        CompanyDTO result = companyService.getCompanyById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getCompanyById_NotFound_ThrowsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> companyService.getCompanyById(99L));
    }

    // ── GET ALL ────────────────────────────────────────────────────────────────

    @Test
    void getAllCompanies_ReturnsAll() {
        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(companyMapper.toDto(company)).thenReturn(companyDTO);

        List<CompanyDTO> result = companyService.getAllCompanies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getAllCompanies_EmptyList_ReturnsEmpty() {
        when(companyRepository.findAll()).thenReturn(List.of());

        List<CompanyDTO> result = companyService.getAllCompanies();

        assertThat(result).isEmpty();
    }

    // ── GET BY CATEGORY ────────────────────────────────────────────────────────

    @Test
    void getCompaniesByCategory_ReturnsFiltered() {
        when(companyRepository.findByCategory("Restaurant")).thenReturn(List.of(company));
        when(companyMapper.toDto(company)).thenReturn(companyDTO);

        List<CompanyDTO> result = companyService.getCompaniesByCategory("Restaurant");

        assertThat(result).hasSize(1);
    }

    @Test
    void getCompaniesByCategory_NoneFound_ReturnsEmpty() {
        when(companyRepository.findByCategory("Unknown")).thenReturn(List.of());

        List<CompanyDTO> result = companyService.getCompaniesByCategory("Unknown");

        assertThat(result).isEmpty();
    }

    // ── GET BY STATUS ──────────────────────────────────────────────────────────

    @Test
    void getCompaniesByStatus_ReturnsFiltered() {
        when(companyRepository.findByStatus("ACTIVE")).thenReturn(List.of(company));
        when(companyMapper.toDto(company)).thenReturn(companyDTO);

        List<CompanyDTO> result = companyService.getCompaniesByStatus("ACTIVE");

        assertThat(result).hasSize(1);
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    @Test
    void updateCompany_Success_SameEmail() {
        // same email → no duplicate check
        CompanyDTO updateDTO = new CompanyDTO();
        updateDTO.setEmail("test@test.com");
        updateDTO.setName("Updated Name");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(companyMapper.toDto(any(Company.class))).thenReturn(companyDTO);

        CompanyDTO result = companyService.updateCompany(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(companyMapper).updateEntityFromDTO(updateDTO, company);
        verify(companyRepository).save(company);
    }

    @Test
    void updateCompany_NewEmailNotTaken_Success() {
        CompanyDTO updateDTO = new CompanyDTO();
        updateDTO.setEmail("new@email.com");
        updateDTO.setName("Updated Name");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(companyMapper.toDto(any(Company.class))).thenReturn(companyDTO);

        CompanyDTO result = companyService.updateCompany(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(companyRepository).save(company);
    }

    @Test
    void updateCompany_DuplicateEmail_ThrowsBusinessException() {
        CompanyDTO updateDTO = new CompanyDTO();
        updateDTO.setEmail("taken@email.com"); // different from existing, but already used somewhere

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.existsByEmail("taken@email.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> companyService.updateCompany(1L, updateDTO));
        verify(companyRepository, never()).save(any());
    }

    @Test
    void updateCompany_NotFound_ThrowsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> companyService.updateCompany(99L, companyDTO));
        verify(companyRepository, never()).save(any());
    }

    // ── ACTIVATE ───────────────────────────────────────────────────────────────

    @Test
    void activateCompany_Success() {
        company.setStatus(CompanyStatus.SUSPENDED);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(companyMapper.toDto(any(Company.class))).thenReturn(companyDTO);

        CompanyDTO result = companyService.activateCompany(1L);

        assertThat(result).isNotNull();
        assertThat(company.getStatus()).isEqualTo(CompanyStatus.ACTIVE);
        verify(companyRepository).save(company);
    }

    @Test
    void activateCompany_NotFound_ThrowsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> companyService.activateCompany(99L));
        verify(companyRepository, never()).save(any());
    }

    // ── SUSPEND ────────────────────────────────────────────────────────────────

    @Test
    void suspendCompany_Success() {
        company.setStatus(CompanyStatus.ACTIVE);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(companyMapper.toDto(any(Company.class))).thenReturn(companyDTO);

        CompanyDTO result = companyService.suspendCompany(1L);

        assertThat(result).isNotNull();
        assertThat(company.getStatus()).isEqualTo(CompanyStatus.SUSPENDED);
        verify(companyRepository).save(company);
    }

    @Test
    void suspendCompany_NotFound_ThrowsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> companyService.suspendCompany(99L));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    @Test
    void deleteCompany_Success() {
        when(companyRepository.existsById(1L)).thenReturn(true);
        doNothing().when(companyRepository).deleteById(1L);

        companyService.deleteCompany(1L);

        verify(companyRepository).deleteById(1L);
    }

    @Test
    void deleteCompany_NotFound_ThrowsResourceNotFoundException() {
        when(companyRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> companyService.deleteCompany(99L));
        verify(companyRepository, never()).deleteById(anyLong());
    }
}
