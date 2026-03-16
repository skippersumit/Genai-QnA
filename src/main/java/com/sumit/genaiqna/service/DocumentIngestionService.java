package com.sumit.genaiqna.service;


import com.sumit.genaiqna.ingestion.IngestionJob;
import com.sumit.genaiqna.service.embedding.EmbeddingService;
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
            float[] vector = null;
            try {
                vector = embeddingService.embedWithTiming(chunk);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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

    public void process(IngestionJob job) throws IOException {

        // 1. Chunk document
        List<String> chunks = chunkText(job.content(), 300);

        // 2. Embed + store
        for (int i = 0; i < chunks.size(); i++) {
            float[] vector;
            try {
                vector = embeddingService.embedWithTiming(chunks.get(i));
            } catch (IOException e) {
                throw new RuntimeException("Failed to embed chunk " + i + " for doc " + job.documentId(), e);
            }

            String pointId = UUID.randomUUID().toString();
            vectorStoreService.upsert(
                    pointId,
                    vector,
                    Map.of(
                            "documentId", job.documentId().toString(),
                            "chunkIndex", i,
                            "text", chunks.get(i)
                    )
            );
        }
    }

}
