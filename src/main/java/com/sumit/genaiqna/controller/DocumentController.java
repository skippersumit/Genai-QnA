package com.sumit.genaiqna.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Operation(summary = "Upload a document")
    @PostMapping
    public ResponseEntity<Map<String, String>> uploadDocument() {
        return ResponseEntity.ok(
                Map.of(
                        "documentId", "doc_dummy",
                        "status", "UPLOADED"
                )
        );
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<Map<String, String>> getDocument(
            @PathVariable String documentId
    ) {
        return ResponseEntity.ok(
                Map.of(
                        "documentId", documentId,
                        "status", "AVAILABLE"
                )
        );
    }
}
