package com.sumit.genaiqna.service.embedding;

import com.sumit.genaiqna.service.EmbeddingParser;
import com.sumit.genaiqna.util.Stopwatch;
import okhttp3.*;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmbeddingService {
    private static final String OLLAMA_URL = "http://localhost:11434/api/embeddings";
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EmbeddingService.class);


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

    @Cacheable(
            value = "embeddings",
            key = "#text"
    )
    public float[] embedWithTiming(String text) throws IOException {
        log.info("Embedding cache MISS for text hash={}", text.hashCode());

        Stopwatch sw = Stopwatch.start();

        String embeddingJson = embed(text);
        float[] vector = EmbeddingParser.extractVector(embeddingJson);

        long timeMs = sw.elapsedMillis();
        log.info("Embedding computed in {} ms", sw.elapsedMillis());

        return vector;
    }

}
