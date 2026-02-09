package com.sumit.genaiqna.service;

import com.sumit.genaiqna.service.llm.LlmClient;
import com.sumit.genaiqna.util.Stopwatch;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AskService {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AskService.class);

    private final CacheManager cacheManager;

    private final SearchService searchService;
    private final LlmClient llmClient;

    public AskService(
            CacheManager cacheManager, SearchService searchService,
            LlmClient llmClient
    ) {
        this.cacheManager = cacheManager;
        this.searchService = searchService;
        this.llmClient = llmClient;
    }

    @Cacheable(
            value = "askResponses",
            key = "#query + ':' + #topK"
    )
    public Map<String, Object> ask(String query, int topK) throws IOException {
        log.info("CACHE MISS → executing /ask for query='{}'", query);

        Stopwatch total = Stopwatch.start();

        List<Map<String, Object>> chunks =
                searchService.search(query, topK);

        String prompt = buildPrompt(query, chunks);

        Map<String, Object> llmResult =
                llmClient.generate(prompt);

        long totalMs = total.elapsedMillis();
        log.info("Total /ask latency: {} ms", totalMs);

        String answer = (String) llmResult.get("answer");

        if ("I don't know".equalsIgnoreCase(answer)) {
            return Map.of(
                    "answer", "I don't know",
                    "sources", List.of()
            );
        }

        List<Integer> citations =
                (List<Integer>) llmResult.get("citations");

        List<Map<String, Object>> sources = citations.stream()
                .filter(i -> i < chunks.size())
                .map(i -> Map.of(
                        "documentId", chunks.get(i).get("documentId"),
                        "chunkIndex", chunks.get(i).get("chunkIndex")
                ))
                .toList();

        return Map.of(
                "answer", answer,
                "sources", sources
        );
    }

    public boolean isCached(String query, int topK) {
        Cache cache = cacheManager.getCache("askResponses");
        return cache != null &&
                cache.get(query + ":" + topK) != null;
    }


    private String buildPrompt(
            String query,
            List<Map<String, Object>> chunks
    ) {

        if (chunks.isEmpty()) {
            return """
                    You must answer: "I don't know".
                    No context is available.
                    Question: %s
                    """.formatted(query);
        }

        StringBuilder context = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {
            context.append("[")
                    .append(i)
                    .append("] ")
                    .append(chunks.get(i).get("text"))
                    .append("\n");
        }

        return """
                You are a backend QA assistant.
                
                RULES:
                - Answer ONLY using the provided context.
                - Do NOT use external knowledge.
                - If the answer is not fully present in the context, respond exactly with: "I don't know".
                
                Context:
                %s
                
                Question:
                %s
                
                Return JSON in the format:
                {
                  "answer": "<answer or I don't know>",
                  "citations": [0, 1]
                }
                """.formatted(context.toString(), query);
    }

}
