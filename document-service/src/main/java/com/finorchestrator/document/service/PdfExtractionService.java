package com.finorchestrator.document.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PdfExtractionService {

    private static final Logger log = LoggerFactory.getLogger(PdfExtractionService.class);

    public Map<Integer, String> extractTextByPage(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        if (filename.endsWith(".pdf")) {
            return extractFromPdf(file.getBytes());
        } else {
            // Text or markdown file fallback
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            Map<Integer, String> pages = new LinkedHashMap<>();
            pages.put(1, text);
            return pages;
        }
    }

    public Map<Integer, String> extractFromPdf(byte[] pdfBytes) throws IOException {
        Map<Integer, String> pages = new LinkedHashMap<>();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            int totalPages = document.getNumberOfPages();
            log.info("Extracting text from PDF with {} pages", totalPages);

            PDFTextStripper stripper = new PDFTextStripper();
            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String pageText = stripper.getText(document).trim();
                pages.put(i, pageText);
            }
        }

        return pages;
    }
}
