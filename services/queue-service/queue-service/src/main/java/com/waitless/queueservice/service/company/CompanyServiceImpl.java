package com.waitless.queueservice.service.company;

import com.waitless.queueservice.dto.CompanyDTO;
import com.waitless.queueservice.entity.Company;
import com.waitless.queueservice.enums.CompanyStatus;
import com.waitless.queueservice.exception.BusinessException;
import com.waitless.queueservice.exception.ResourceNotFoundException;
import com.waitless.queueservice.mapper.CompanyMapper;
import com.waitless.queueservice.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    public final CompanyRepository companyRepository;
    public final CompanyMapper companyMapper;


    public CompanyDTO createCompany(CompanyDTO companyDTO){

        if(companyRepository.existsByEmail(companyDTO.getEmail())){
            throw new BusinessException("Email already exists");
        }
        Company company = companyMapper.toEntity(companyDTO);
        companyRepository.save(company);
        return companyMapper.toDto(company);
    }


    public List<CompanyDTO> getAllCompanies(){

      return  companyRepository.findAll().stream()
              .map(companyMapper::toDto)
              .collect(Collectors.toList());

    }

    public CompanyDTO getCompanyById(Long id){
       Company company = companyRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
       return companyMapper.toDto(company);
    }
    public List<CompanyDTO> getCompaniesByCategory(String category){
        return companyRepository.findByCategory(category).stream()
                .map(companyMapper::toDto)
                .collect(Collectors.toList());
    }
    public List<CompanyDTO> getCompaniesByStatus(String status){

        return companyRepository.findByStatus(status).stream()
                .map(companyMapper::toDto)
                .collect(Collectors.toList());
    }
    public CompanyDTO updateCompany(Long id, CompanyDTO companyDTO){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (!company.getEmail().equals(companyDTO.getEmail())
                && companyRepository.existsByEmail(companyDTO.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé par une autre entreprise");
        }

        companyMapper.updateEntityFromDTO(companyDTO,company);
        Company updated = companyRepository.save(company);
        return companyMapper.toDto(updated);

    }

    public CompanyDTO activateCompany(Long id){

        log.info("Starting activation for company ID: {}", id);
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        log.info("Found company: {}, current status: {}", company.getName(), company.getStatus());
        company.setStatus(CompanyStatus.ACTIVE);
        
        log.info("Saving company...");
        Company saved = companyRepository.save(company);
        
        log.info("Company saved successfully. Mapping to DTO...");
        return companyMapper.toDto(saved);

    }

    public CompanyDTO suspendCompany(Long id){
        Company company =companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        company.setStatus(CompanyStatus.SUSPENDED);
        companyRepository.save(company);
        return companyMapper.toDto(company);
    }

    public void deleteCompany(Long id){
        if(!companyRepository.existsById(id)){
            throw new ResourceNotFoundException("Company not found");
        }
        companyRepository.deleteById(id);
    }
}
