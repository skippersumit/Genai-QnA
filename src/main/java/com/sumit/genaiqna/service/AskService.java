package com.sumit.genaiqna.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AskService {

    private final SearchService searchService;
    private final LlmService llmService;

    public AskService(
            SearchService searchService,
            LlmService llmService
    ) {
        this.searchService = searchService;
        this.llmService = llmService;
    }

    public Map<String, Object> ask(String query, int topK) throws IOException {

        // 1️⃣ Retrieve relevant chunks
        List<Map<String, Object>> chunks =
                searchService.search(query, topK);

        // 2️⃣ Build grounded prompt
        String prompt = buildPrompt(query, chunks);

        // 3️⃣ Call LLM
        String answer = llmService.generate(prompt);

        // 4️⃣ Attach sources
        List<Map<String, Object>> sources = chunks.stream()
                .map(c -> Map.of(
                        "documentId", c.get("documentId"),
                        "chunkIndex", c.get("chunkIndex")
                ))
                .toList();

        return Map.of(
                "answer", answer,
                "sources", sources
        );
    }

    private String buildPrompt(
            String query,
            List<Map<String, Object>> chunks
    ) {

        StringBuilder context = new StringBuilder();

        for (Map<String, Object> chunk : chunks) {
            context.append("- ")
                    .append(chunk.get("text"))
                    .append("\n");
        }

        return """
                You are a backend QA assistant.
                Answer the question ONLY using the context below.
                If the answer is not present in the context, say "I don't know".
                
                Context:
                %s
                
                Question:
                %s
                """.formatted(context.toString(), query);
    }
}
