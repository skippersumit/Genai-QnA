package com.sumit.genaiqna.controller;

import com.sumit.genaiqna.service.LlmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/llm")
public class LlmTestController {
    private final LlmService llmService;

    public LlmTestController(LlmService llmService) {
        this.llmService = llmService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testLlm(
            @RequestParam(defaultValue = "Say hello from LLM") String prompt
    ) throws Exception {
        return ResponseEntity.ok(llmService.testCall(prompt));
    }

    @GetMapping("/structured-test")
    public ResponseEntity<String> structuredTest(
            @RequestParam String question
    ) throws Exception {
        String response = llmService.testStructuredCall(question);
        return ResponseEntity.ok(response);
    }
}
