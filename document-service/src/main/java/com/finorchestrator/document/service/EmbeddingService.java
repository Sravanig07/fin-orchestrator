package com.finorchestrator.document.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    public static final int EMBEDDING_DIMENSION = 384;

    public List<Float> generateEmbedding(String text) {
        float[] vector = new float[EMBEDDING_DIMENSION];
        if (text == null || text.isBlank()) {
            return toList(vector);
        }

        String[] tokens = text.toLowerCase().split("[^a-zA-Z0-9]+");
        for (String token : tokens) {
            if (token.isBlank()) continue;
            int hash = Math.abs(token.hashCode());
            int idx1 = hash % EMBEDDING_DIMENSION;
            int idx2 = (hash / 31) % EMBEDDING_DIMENSION;

            vector[idx1] += 1.0f;
            vector[idx2] += 0.5f;
        }

        // L2 Normalization
        float norm = 0.0f;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);

        if (norm > 0.0f) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }

        return toList(vector);
    }

    public double calculateCosineSimilarity(List<Float> v1, List<Float> v2) {
        if (v1 == null || v2 == null || v1.size() != v2.size()) return 0.0;
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            normA += v1.get(i) * v1.get(i);
            normB += v2.get(i) * v2.get(i);
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private List<Float> toList(float[] arr) {
        List<Float> list = new ArrayList<>(arr.length);
        for (float v : arr) {
            list.add(v);
        }
        return list;
    }
}
