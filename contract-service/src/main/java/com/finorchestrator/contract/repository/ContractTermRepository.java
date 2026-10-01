package com.finorchestrator.contract.repository;

import com.finorchestrator.contract.entity.ContractTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractTermRepository extends JpaRepository<ContractTerm, Long> {
    List<ContractTerm> findByContractId(Long contractId);
    List<ContractTerm> findByContractIdAndTermKey(Long contractId, String termKey);
}
