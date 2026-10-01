package com.finorchestrator.contract.service.impl;

import com.finorchestrator.common.event.ContractUploadedEvent;
import com.finorchestrator.common.event.DomainEvent;
import com.finorchestrator.common.exception.ResourceNotFoundException;
import com.finorchestrator.contract.dto.*;
import com.finorchestrator.contract.entity.Contract;
import com.finorchestrator.contract.entity.ContractClause;
import com.finorchestrator.contract.entity.ContractTerm;
import com.finorchestrator.contract.repository.ContractClauseRepository;
import com.finorchestrator.contract.repository.ContractRepository;
import com.finorchestrator.contract.repository.ContractTermRepository;
import com.finorchestrator.contract.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private static final Logger log = LoggerFactory.getLogger(ContractServiceImpl.class);
    private static final String CONTRACTS_TOPIC = "finorchestrator.contracts.v1";

    private final ContractRepository contractRepository;
    private final ContractTermRepository termRepository;
    private final ContractClauseRepository clauseRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<ContractDto> getContracts(Long userId) {
        return contractRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDto> searchContracts(Long userId, String query) {
        return contractRepository.searchContracts(userId, query).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDto getContract(Long id, Long userId) {
        Contract contract = contractRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));
        return mapToDto(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractTermDto> getContractTerms(Long contractId, Long userId) {
        // Validate ownership
        contractRepository.findByIdAndUserId(contractId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        return termRepository.findByContractId(contractId).stream()
                .map(this::mapTermToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractClauseDto> getContractClauses(Long contractId, Long userId) {
        contractRepository.findByIdAndUserId(contractId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        return clauseRepository.findByContractId(contractId).stream()
                .map(this::mapClauseToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractClauseDto> getContractPenalties(Long contractId, Long userId) {
        contractRepository.findByIdAndUserId(contractId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        return clauseRepository.findByContractIdAndClauseType(contractId, "PENALTY").stream()
                .map(this::mapClauseToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractDto> getExpiringContracts(Long userId, int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(daysAhead);

        return contractRepository.findExpiringContracts(userId, today, cutoff).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContractDto createContract(Long userId, CreateContractRequest request) {
        Contract contract = Contract.builder()
                .userId(userId)
                .title(request.getTitle())
                .contractType(request.getContractType().toUpperCase())
                .counterparty(request.getCounterparty())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .renewalDate(request.getRenewalDate())
                .documentId(request.getDocumentId())
                .filePath(request.getFilePath())
                .status("ACTIVE")
                .version(1)
                .build();

        contract = contractRepository.save(contract);

        if (request.getTerms() != null) {
            for (ContractTermDto t : request.getTerms()) {
                ContractTerm term = ContractTerm.builder()
                        .contractId(contract.getId())
                        .termKey(t.getTermKey())
                        .termValue(t.getTermValue())
                        .unit(t.getUnit())
                        .description(t.getDescription())
                        .confidenceScore(t.getConfidenceScore())
                        .build();
                termRepository.save(term);
            }
        }

        if (request.getClauses() != null) {
            for (ContractClauseDto c : request.getClauses()) {
                ContractClause clause = ContractClause.builder()
                        .contractId(contract.getId())
                        .clauseTitle(c.getClauseTitle())
                        .sectionNumber(c.getSectionNumber())
                        .pageNumber(c.getPageNumber())
                        .chunkId(c.getChunkId())
                        .clauseType(c.getClauseType())
                        .clauseText(c.getClauseText())
                        .build();
                clauseRepository.save(clause);
            }
        }

        // Emit Kafka event
        publishContractUploadedEvent(contract);

        return mapToDto(contract);
    }

    private void publishContractUploadedEvent(Contract c) {
        try {
            ContractUploadedEvent payload = ContractUploadedEvent.builder()
                    .contractId(c.getId())
                    .userId(c.getUserId())
                    .title(c.getTitle())
                    .contractType(c.getContractType())
                    .counterparty(c.getCounterparty())
                    .documentId(c.getDocumentId())
                    .filePath(c.getFilePath())
                    .build();

            DomainEvent<ContractUploadedEvent> event = DomainEvent.of("CONTRACT_UPLOADED", "contract-service", payload);
            kafkaTemplate.send(CONTRACTS_TOPIC, String.valueOf(c.getUserId()), event);
            log.info("Emitted CONTRACT_UPLOADED event to Kafka for contractId: {}", c.getId());
        } catch (Exception e) {
            log.warn("Kafka emission for contract failed gracefully: {}", e.getMessage());
        }
    }

    private ContractDto mapToDto(Contract c) {
        List<ContractTermDto> terms = termRepository.findByContractId(c.getId()).stream()
                .map(this::mapTermToDto)
                .collect(Collectors.toList());

        List<ContractClauseDto> clauses = clauseRepository.findByContractId(c.getId()).stream()
                .map(this::mapClauseToDto)
                .collect(Collectors.toList());

        return ContractDto.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .title(c.getTitle())
                .contractType(c.getContractType())
                .counterparty(c.getCounterparty())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .renewalDate(c.getRenewalDate())
                .status(c.getStatus())
                .version(c.getVersion())
                .documentId(c.getDocumentId())
                .filePath(c.getFilePath())
                .terms(terms)
                .clauses(clauses)
                .createdAt(c.getCreatedAt())
                .build();
    }

    private ContractTermDto mapTermToDto(ContractTerm t) {
        return ContractTermDto.builder()
                .id(t.getId())
                .contractId(t.getContractId())
                .termKey(t.getTermKey())
                .termValue(t.getTermValue())
                .unit(t.getUnit())
                .description(t.getDescription())
                .confidenceScore(t.getConfidenceScore())
                .build();
    }

    private ContractClauseDto mapClauseToDto(ContractClause c) {
        return ContractClauseDto.builder()
                .id(c.getId())
                .contractId(c.getContractId())
                .clauseTitle(c.getClauseTitle())
                .sectionNumber(c.getSectionNumber())
                .pageNumber(c.getPageNumber())
                .chunkId(c.getChunkId())
                .clauseType(c.getClauseType())
                .clauseText(c.getClauseText())
                .build();
    }
}
