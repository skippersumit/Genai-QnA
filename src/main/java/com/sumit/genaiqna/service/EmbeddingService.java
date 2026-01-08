package com.sumit.genaiqna.service;

import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmbeddingService {
    private static final String OLLAMA_URL = "http://localhost:11434/api/embeddings";
    private final OkHttpClient client = new OkHttpClient();


    public String embed(String text) throws IOException {

        String requestBody = """
                {
                  "model": "nomic-embed-text",
                  "prompt": "%s"
                }
                """.formatted(text);

        Request request = new Request.Builder()
                .url(OLLAMA_URL)
                .post(RequestBody.create(
                        requestBody,
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Ollama Error: " + response);
            return response.body().string();
        }
    }
}
