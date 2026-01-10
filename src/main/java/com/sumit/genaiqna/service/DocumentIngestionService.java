package com.sumit.genaiqna.service;


import com.sumit.genaiqna.service.vector.VectorStoreService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentIngestionService {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public DocumentIngestionService(
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService
    ) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    public String ingest(String text) {

        String documentId = UUID.randomUUID().toString();

        List<String> chunks = chunkText(text, 300);

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);

            String embeddingJson = null;
            try {
                embeddingJson = embeddingService.embed(chunk);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            float[] vector = EmbeddingParser.extractVector(embeddingJson);

            vectorStoreService.upsert(
                    UUID.randomUUID().toString(),
                    vector,
                    Map.of(
                            "documentId", documentId,
                            "chunkIndex", i,
                            "text", chunk
                    )
            );
        }

        return documentId;
    }

    // Simple fixed-size chunking
    private List<String> chunkText(String text, int chunkSize) {

        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(text.length(), i + chunkSize);
            chunks.add(text.substring(i, end));
        }

        return chunks;
    }
}
