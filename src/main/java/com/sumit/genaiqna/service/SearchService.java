package com.sumit.genaiqna.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumit.genaiqna.service.vector.VectorStoreService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    private final ObjectMapper mapper = new ObjectMapper();

    public SearchService(
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService
    ) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    public List<Map<String, Object>> search(String query, int topK) {

        // 1️⃣ Generate embedding for query
        float[] queryVector;
        try {
            queryVector = embeddingService.embedWithTiming(query);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        float[] queryVector = EmbeddingParser.extractVector(embeddingJson);

        // 2️⃣ Search vector DB
        List<Map<String, Object>> rawResults =
                vectorStoreService.search(queryVector, topK);

        // 3️⃣ Parse raw Qdrant response
        return extractChunks(rawResults);
    }

    private List<Map<String, Object>> extractChunks(
            List<Map<String, Object>> rawResults
    ) {
        try {
            String json = (String) rawResults.get(0).get("raw");
            JsonNode root = mapper.readTree(json);
            JsonNode resultArray = root.get("result");

            List<Map<String, Object>> chunks = new ArrayList<>();

            for (JsonNode node : resultArray) {
                chunks.add(
                        Map.of(
                                "score", node.get("score").asDouble(),
                                "text", node.get("payload").get("text").asText(),
                                "documentId", node.get("payload").get("documentId").asText(),
                                "chunkIndex", node.get("payload").get("chunkIndex").asInt()
                        )
                );
            }
            return chunks;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse search results", e);
        }
    }
}
