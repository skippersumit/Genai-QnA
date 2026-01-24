package com.sumit.genaiqna.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumit.genaiqna.service.prompt.OutputSchema;
import com.sumit.genaiqna.service.prompt.SystemPrompt;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class LlmClient {
    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();
    @Value("${groq.api.key}")
    private String apiKey;

    public String testCall(String prompt) throws IOException {

        String requestBody = """
                {
                  "model": "llama-3.1-8b-instant",
                  "messages": [
                    { "role": "user", "content": "%s" }
                  ],
                  "temperature": 0.2
                }
                """.formatted(prompt);

        Request request = new Request.Builder()
                .url(GROQ_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody,
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public Map<String, Object> generate(String question) throws IOException {
        String escapedQuestion = question
                .replace("\\", "\\\\")   // Escape backslashes first
                .replace("\"", "\\\"")   // Escape double quotes
                .replace("\n", "\\n")    // Escape newlines
                .replace("\r", "\\r");   // Escape carriage returns
        String requestBody = """
                {
                  "model": "llama-3.1-8b-instant",
                  "messages": [
                    { "role": "system", "content": "%s" },
                    { "role": "user", "content": "%s" },
                    { "role": "user", "content": "%s" }
                  ],
                  "temperature": 0
                }
                """.formatted(
                escapeForJson(SystemPrompt.value()),
                escapeForJson(OutputSchema.instruction()),
                escapedQuestion
        );


        // same HTTP call logic

        Request request = new Request.Builder()
                .url(GROQ_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody,
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String rawResponse = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);

            // Step 1: extract message content
            String content =
                    root.get("choices")
                            .get(0)
                            .get("message")
                            .get("content")
                            .asText();

            // Step 2: parse structured JSON inside content
            JsonNode structured = mapper.readTree(content);

            // Step 3: return only the final answer
            return Map.of(
                    "answer", structured.get("answer").asText(),
                    "citations", mapper.convertValue(
                            structured.get("citations"), List.class
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse grounded LLM response", e);
        }
    }

    private String escapeForJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
