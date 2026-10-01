package com.finorchestrator.document.service;

import com.finorchestrator.document.dto.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);

    private final EmbeddingService embeddingService;
    private final Map<String, DocumentChunk> chunkStorage = new ConcurrentHashMap<>();

    @Value("${qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${qdrant.port:6333}")
    private int qdrantPort;

    public VectorStoreService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
        // Initialize default seed chunks for test contracts (Home Loan, Star Health, Amex)
        initializeSeedChunks();
    }

    public void indexChunks(List<DocumentChunk> chunks) {
        for (DocumentChunk chunk : chunks) {
            if (chunk.getEmbedding() == null) {
                chunk.setEmbedding(embeddingService.generateEmbedding(chunk.getText()));
            }
            chunkStorage.put(chunk.getChunkId(), chunk);
        }
        log.info("Indexed {} chunks in vector store (total: {})", chunks.size(), chunkStorage.size());
    }

    public List<DocumentChunk> search(String query, Long contractId, String documentId, int topK, double minScore) {
        List<Float> queryEmbedding = embeddingService.generateEmbedding(query);

        return chunkStorage.values().stream()
                .filter(chunk -> contractId == null || Objects.equals(chunk.getContractId(), contractId))
                .filter(chunk -> documentId == null || Objects.equals(chunk.getDocumentId(), documentId))
                .map(chunk -> {
                    double similarity = embeddingService.calculateCosineSimilarity(queryEmbedding, chunk.getEmbedding());
                    return DocumentChunk.builder()
                            .chunkId(chunk.getChunkId())
                            .documentId(chunk.getDocumentId())
                            .contractId(chunk.getContractId())
                            .title(chunk.getTitle())
                            .pageNumber(chunk.getPageNumber())
                            .sectionNumber(chunk.getSectionNumber())
                            .clauseType(chunk.getClauseType())
                            .text(chunk.getText())
                            .score(Math.round(similarity * 100.0) / 100.0)
                            .build();
                })
                .filter(chunk -> chunk.getScore() >= minScore)
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    public List<DocumentChunk> getChunksByDocumentId(String documentId) {
        return chunkStorage.values().stream()
                .filter(c -> Objects.equals(c.getDocumentId(), documentId))
                .collect(Collectors.toList());
    }

    private void initializeSeedChunks() {
        List<DocumentChunk> seed = List.of(
                DocumentChunk.builder()
                        .chunkId("CHUNK-DOC-HDFC-HL-2024-P14-01")
                        .documentId("DOC-HDFC-HL-2024")
                        .contractId(1L)
                        .title("HDFC Home Loan Facility Agreement")
                        .pageNumber(14)
                        .sectionNumber("Section 8.2")
                        .clauseType("PENALTY")
                        .text("As per Reserve Bank of India (RBI) guidelines, no prepayment charges or penalty shall be levied on early repayment of floating rate term loans sanctioned to individual borrowers, whether in partial or full settlement.")
                        .build(),
                DocumentChunk.builder()
                        .chunkId("CHUNK-DOC-HDFC-HL-2024-P19-01")
                        .documentId("DOC-HDFC-HL-2024")
                        .contractId(1L)
                        .title("HDFC Home Loan Facility Agreement")
                        .pageNumber(19)
                        .sectionNumber("Section 11.4")
                        .clauseType("PENALTY")
                        .text("In the event of default in payment of EMI on the scheduled due date, the Borrower shall be liable to pay penal interest at the rate of 2% per month (24% per annum) on the overdue sum from the due date until actual payment.")
                        .build(),
                DocumentChunk.builder()
                        .chunkId("CHUNK-DOC-STAR-HEALTH-2025-P08-01")
                        .documentId("DOC-STAR-HEALTH-2025")
                        .contractId(2L)
                        .title("Star Health Comprehensive Insurance Policy")
                        .pageNumber(8)
                        .sectionNumber("Section 4.1")
                        .clauseType("RENEWAL")
                        .text("A grace period of thirty (30) days from the date of expiry is permissible for renewal of the policy. During the grace period, payment shall maintain continuity of waiting periods, but claims incurred during the unpaid grace interval will not be admissible.")
                        .build(),
                DocumentChunk.builder()
                        .chunkId("CHUNK-DOC-AMEX-PLAT-2023-P04-01")
                        .documentId("DOC-AMEX-PLAT-2023")
                        .contractId(3L)
                        .title("American Express Cardmember Agreement")
                        .pageNumber(4)
                        .sectionNumber("Clause 5(b)")
                        .clauseType("FEE")
                        .text("If the Total Amount Due is not paid by the Payment Due Date, finance charges at the rate of 3.75% per month (annualized rate 45.00%) shall apply on daily balances from the date of transaction until settlement, in addition to a Late Payment Fee of INR 1,200.")
                        .build()
        );

        indexChunks(seed);
    }
}
