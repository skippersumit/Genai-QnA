package com.sumit.genaiqna.controller;

import com.sumit.genaiqna.service.AskService;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/ask")
public class AskController {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AskController.class);

    private final AskService askService;

    public AskController(AskService askService) {
        this.askService = askService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> ask(
            @RequestBody Map<String, Object> body
    ) throws IOException {
        String query = (String) body.get("query");
        int topK = body.get("topK") == null ? 3 : (int) body.get("topK");

        if (askService.isCached(query, topK)) {
            log.info("CACHE HIT for /ask");
        }

        return ResponseEntity.ok(
                askService.ask(query, topK)
        );
    }
}
