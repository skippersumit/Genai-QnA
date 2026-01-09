package com.sumit.genaiqna.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EmbeddingParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static float[] extractVector(String embeddingJson) {
        try {
            JsonNode root = mapper.readTree(embeddingJson);
            JsonNode embeddingNode = root.get("embedding");

            float[] vector = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vector[i] = embeddingNode.get(i).floatValue();
            }
            return vector;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse embedding", e);
        }
    }
}
