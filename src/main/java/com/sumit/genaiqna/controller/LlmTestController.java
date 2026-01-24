package com.sumit.genaiqna.controller;

import com.sumit.genaiqna.service.llm.LlmClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/llm")
public class LlmTestController {
    private final LlmClient llmClient;

    public LlmTestController(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testLlm(
            @RequestParam(defaultValue = "Say hello from LLM") String prompt
    ) throws Exception {
        return ResponseEntity.ok(llmClient.testCall(prompt));
    }

    @GetMapping("/structured-test")
    public ResponseEntity<String> structuredTest(
            @RequestParam String question
    ) throws Exception {
        Map<String, Object> response = llmClient.generate(question);
        return ResponseEntity.ok(response.toString());
    }
}
