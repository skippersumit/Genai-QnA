package com.sumit.genaiqna.service;

import com.sumit.genaiqna.service.prompt.OutputSchema;
import com.sumit.genaiqna.service.prompt.SystemPrompt;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LlmService {
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

    public String testStructuredCall(String question) throws IOException {
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
            return response.body().string();
        }
    }

    private String escapeForJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
