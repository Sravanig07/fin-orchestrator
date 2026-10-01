package com.finorchestrator.document.service;

import com.finorchestrator.document.dto.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChunkingService {

    private static final int TARGET_CHUNK_WORDS = 250;
    private static final int OVERLAP_WORDS = 40;

    private static final Pattern SECTION_PATTERN = Pattern.compile("(?i)(section|clause|article)\\s+([0-9]+(\\.[0-9]+)*)");
    private static final Pattern PENALTY_PATTERN = Pattern.compile("(?i)(penalty|penal|late fee|default|forfeiture|overdue)");
    private static final Pattern FEE_PATTERN = Pattern.compile("(?i)(fee|charge|commission|annual charge|processing fee)");
    private static final Pattern OBLIGATION_PATTERN = Pattern.compile("(?i)(obligation|shall pay|shall provide|covenant|minimum amount)");
    private static final Pattern RENEWAL_PATTERN = Pattern.compile("(?i)(renewal|grace period|expiry|termination|notice period)");

    public List<DocumentChunk> chunkDocument(String documentId, Long contractId, String title, Map<Integer, String> pages) {
        List<DocumentChunk> allChunks = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : pages.entrySet()) {
            int pageNumber = entry.getKey();
            String pageText = entry.getValue();

            String[] words = pageText.split("\\s+");
            if (words.length == 0 || pageText.isBlank()) continue;

            int start = 0;
            int chunkIndex = 1;

            while (start < words.length) {
                int end = Math.min(start + TARGET_CHUNK_WORDS, words.length);

                StringBuilder chunkBuilder = new StringBuilder();
                for (int i = start; i < end; i++) {
                    chunkBuilder.append(words[i]).append(" ");
                }
                String chunkText = chunkBuilder.toString().trim();

                String sectionNumber = detectSection(chunkText);
                String clauseType = detectClauseType(chunkText);
                String chunkId = String.format("CHUNK-%s-P%02d-%02d", documentId, pageNumber, chunkIndex++);

                DocumentChunk chunk = DocumentChunk.builder()
                        .chunkId(chunkId)
                        .documentId(documentId)
                        .contractId(contractId)
                        .title(title)
                        .pageNumber(pageNumber)
                        .sectionNumber(sectionNumber)
                        .clauseType(clauseType)
                        .text(chunkText)
                        .build();

                allChunks.add(chunk);

                if (end >= words.length) break;
                start = end - OVERLAP_WORDS;
            }
        }

        return allChunks;
    }

    private String detectSection(String text) {
        Matcher matcher = SECTION_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "General Section";
    }

    private String detectClauseType(String text) {
        if (PENALTY_PATTERN.matcher(text).find()) return "PENALTY";
        if (FEE_PATTERN.matcher(text).find()) return "FEE";
        if (RENEWAL_PATTERN.matcher(text).find()) return "RENEWAL";
        if (OBLIGATION_PATTERN.matcher(text).find()) return "OBLIGATION";
        return "GENERAL";
    }
}
