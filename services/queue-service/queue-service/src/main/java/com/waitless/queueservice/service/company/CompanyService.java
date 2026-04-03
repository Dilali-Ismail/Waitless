package com.waitless.queueservice.service.company;

import com.waitless.queueservice.dto.CompanyDTO;

import java.util.List;

public interface CompanyService {
    public CompanyDTO createCompany(CompanyDTO companyDTO);
    public List<CompanyDTO> getAllCompanies();
    public CompanyDTO getCompanyById(Long id);
    public List<CompanyDTO> getCompaniesByCategory(String category);
    public List<CompanyDTO> getCompaniesByStatus(String status);
    public CompanyDTO updateCompany(Long id, CompanyDTO companyDTO);
    public CompanyDTO activateCompany(Long id);
    public CompanyDTO suspendCompany(Long id);
    public void deleteCompany(Long id);
}
