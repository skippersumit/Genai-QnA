package com.sumit.genaiqna.controller;


import com.sumit.genaiqna.ingestion.IngestionJob;
import com.sumit.genaiqna.ingestion.IngestionQueue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
public class DocumentIngestionController {

    //    private final DocumentIngestionService ingestionService;
    private final IngestionQueue ingestionQueue;

    public DocumentIngestionController(IngestionQueue ingestionQueue) {
//        this.ingestionService = ingestionService;
        this.ingestionQueue = ingestionQueue;
    }

//    @PostMapping("/ingest")
//    public ResponseEntity<Map<String, Object>> ingest(
//            @RequestBody Map<String, String> body
//    ) {
//        String text = body.get("text");
//
//        String documentId = ingestionService.ingest(text);
//
//        return ResponseEntity.ok(
//                Map.of(
//                        "documentId", documentId,
//                        "status", "INGESTED"
//                )
//        );
//    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(
            @RequestBody Map<String, String> body
    ) {
        UUID documentId = UUID.randomUUID();
        String content = body.get("content");

        ingestionQueue.submit(
                new IngestionJob(documentId, content)
        );

        return ResponseEntity.accepted().body(
                Map.of(
                        "documentId", documentId,
                        "status", "INGESTION_STARTED"
                )
        );
    }
}
