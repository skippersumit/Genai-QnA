package com.sumit.genaiqna.controller;

import com.sumit.genaiqna.service.embedding.EmbeddingService;
import com.sumit.genaiqna.service.vector.VectorStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/vector-test")
public class VectorTestController {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public VectorTestController(
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService
    ) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    @GetMapping("/run")
    public ResponseEntity<String> runTest() throws Exception {

        // 1️⃣ Generate embedding for insert text
        String insertText = "Spring Boot is a Java framework";
        float[] insertVector =
                embeddingService.embedWithTiming(insertText);

        // 2️⃣ Insert into Qdrant
        String pointId = UUID.randomUUID().toString();

        vectorStoreService.upsert(
                pointId,
                insertVector,
                Map.of("text", insertText)
        );


        // 3️⃣ Generate embedding for query text
        String queryText = "Java backend framework";

        float[] queryVector = embeddingService.embedWithTiming(queryText);

        // 4️⃣ Search in Qdrant
        var results = vectorStoreService.search(queryVector, 1);

        return ResponseEntity.ok(
                "Vector insert + search executed. Results: " + results
        );
    }
}


