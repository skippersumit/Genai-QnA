package com.sumit.genaiqna.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/queries")
public class QueryController {
    @PostMapping
    public ResponseEntity<Map<String, String>> query() {
        return ResponseEntity.ok(
                Map.of(
                        "queryId", "qry_dummy",
                        "status", "PROCESSING"
                )
        );
    }

    @GetMapping("/{queryId}")
    public ResponseEntity<Map<String, String>> getQueryResult(
            @PathVariable String queryId
    ) {
        return ResponseEntity.ok(
                Map.of(
                        "queryId", queryId,
                        "answer", "This is a stub response"
                )
        );
    }

}
