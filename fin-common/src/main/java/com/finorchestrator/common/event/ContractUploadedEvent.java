package com.finorchestrator.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractUploadedEvent {
    private Long contractId;
    private Long userId;
    private String title;
    private String contractType;
    private String counterparty;
    private String documentId;
    private String filePath;
}
