package com.finorchestrator.contract.repository;

import com.finorchestrator.contract.entity.ContractClause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractClauseRepository extends JpaRepository<ContractClause, Long> {
    List<ContractClause> findByContractId(Long contractId);
    List<ContractClause> findByContractIdAndClauseType(Long contractId, String clauseType);
}
