package com.sumit.genaiqna.controller;


import com.sumit.genaiqna.service.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/documents")
public class DocumentIngestionController {

    private final DocumentIngestionService ingestionService;

    public DocumentIngestionController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(
            @RequestBody Map<String, String> body
    ) {
        String text = body.get("text");

        String documentId = ingestionService.ingest(text);

        return ResponseEntity.ok(
                Map.of(
                        "documentId", documentId,
                        "status", "INGESTED"
                )
        );
    }
}
