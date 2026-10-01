package com.finorchestrator.contract.service;

import com.finorchestrator.contract.dto.*;

import java.util.List;

public interface ContractService {
    List<ContractDto> getContracts(Long userId);
    List<ContractDto> searchContracts(Long userId, String query);
    ContractDto getContract(Long id, Long userId);
    List<ContractTermDto> getContractTerms(Long contractId, Long userId);
    List<ContractClauseDto> getContractClauses(Long contractId, Long userId);
    List<ContractClauseDto> getContractPenalties(Long contractId, Long userId);
    List<ContractDto> getExpiringContracts(Long userId, int daysAhead);
    ContractDto createContract(Long userId, CreateContractRequest request);
}
