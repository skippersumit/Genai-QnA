package com.sumit.genaiqna.controller;


import com.sumit.genaiqna.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> search(
            @RequestBody Map<String, Object> body
    ) {
        String query = (String) body.get("query");
        int topK = body.get("topK") == null ? 3 : (int) body.get("topK");

        return ResponseEntity.ok(
                Map.of("results", searchService.search(query, topK))
        );
    }
}
