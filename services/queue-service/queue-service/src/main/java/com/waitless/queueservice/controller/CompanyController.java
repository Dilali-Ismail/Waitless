package com.waitless.queueservice.controller;


import com.waitless.queueservice.dto.CompanyDTO;
import com.waitless.queueservice.enums.CompanyStatus;
import com.waitless.queueservice.service.company.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyDTO> createCompany(
            @Valid
            @RequestBody CompanyDTO companyDTO){

        CompanyDTO company = companyService.createCompany(companyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<List<CompanyDTO>> getAllCompanies(){
        List<CompanyDTO> companyDTOs = companyService.getAllCompanies();
        return ResponseEntity.ok(companyDTOs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<CompanyDTO> getCompany(@PathVariable Long id){
        CompanyDTO companyDTO = companyService.getCompanyById(id);
        return ResponseEntity.ok(companyDTO);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<List<CompanyDTO>> getCompaniesByStatus(@PathVariable String status){
        List<CompanyDTO> companyDTOs = companyService.getCompaniesByStatus(status);
        return ResponseEntity.ok(companyDTOs);
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<List<CompanyDTO>> getCompaniesByCategory(@PathVariable String category){
        List<CompanyDTO> companyDTOs = companyService.getCompaniesByCategory(category);
        return ResponseEntity.ok(companyDTOs);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<CompanyDTO> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyDTO companyDTO){
        CompanyDTO company = companyService.updateCompany(id, companyDTO);
        return ResponseEntity.ok(company);
    }
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyDTO> activateCompany(@PathVariable Long id){
        CompanyDTO companyDTO = companyService.activateCompany(id);
        return ResponseEntity.ok(companyDTO);
    }
    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyDTO> suspendCompany(@PathVariable Long id){
        CompanyDTO companyDTO = companyService.suspendCompany(id);
        return ResponseEntity.ok(companyDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletCompany(@PathVariable Long id){
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }










}
