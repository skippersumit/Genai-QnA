package com.sumit.genaiqna.controller;

import com.sumit.genaiqna.service.embedding.EmbeddingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/embed")
public class EmbeddingController {
    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping
    public ResponseEntity<float[]> embed(
            @RequestBody Map<String, String> body
    ) throws Exception {
        return ResponseEntity.ok(
                embeddingService.embedWithTiming(body.get("text"))
        );
    }
}
