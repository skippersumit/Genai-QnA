package com.sumit.genaiqna.service;

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
}
