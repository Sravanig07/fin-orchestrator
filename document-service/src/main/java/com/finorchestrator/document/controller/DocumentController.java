package com.finorchestrator.document.controller;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.document.dto.DocumentChunk;
import com.finorchestrator.document.dto.DocumentSearchRequest;
import com.finorchestrator.document.dto.DocumentSearchResponse;
import com.finorchestrator.document.service.ChunkingService;
import com.finorchestrator.document.service.PdfExtractionService;
import com.finorchestrator.document.service.VectorStoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final PdfExtractionService extractionService;
    private final ChunkingService chunkingService;
    private final VectorStoreService vectorStoreService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "contractId", required = false) Long contractId,
            @RequestParam(value = "title", required = false) String title) throws IOException {

        String documentId = "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String docTitle = title != null ? title : file.getOriginalFilename();

        // 1. Extract text page by page
        Map<Integer, String> pages = extractionService.extractTextByPage(file);

        // 2. Chunk into structured units
        List<DocumentChunk> chunks = chunkingService.chunkDocument(documentId, contractId, docTitle, pages);

        // 3. Index embeddings into vector store
        vectorStoreService.indexChunks(chunks);

        Map<String, Object> result = Map.of(
                "documentId", documentId,
                "title", docTitle,
                "pagesExtracted", pages.size(),
                "chunksIndexed", chunks.size(),
                "status", "INDEXED"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Document processed and indexed successfully", result));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<DocumentSearchResponse>> search(
            @Valid @RequestBody DocumentSearchRequest request) {

        List<DocumentChunk> chunks = vectorStoreService.search(
                request.getQuery(),
                request.getContractId(),
                request.getDocumentId(),
                request.getTopK(),
                request.getMinScore()
        );

        DocumentSearchResponse response = DocumentSearchResponse.builder()
                .query(request.getQuery())
                .totalMatches(chunks.size())
                .chunks(chunks)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{documentId}/chunks")
    public ResponseEntity<ApiResponse<List<DocumentChunk>>> getChunks(@PathVariable("documentId") String documentId) {
        List<DocumentChunk> chunks = vectorStoreService.getChunksByDocumentId(documentId);
        return ResponseEntity.ok(ApiResponse.ok(chunks));
    }
}
