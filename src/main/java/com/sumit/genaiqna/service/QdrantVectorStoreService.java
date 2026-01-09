package com.sumit.genaiqna.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumit.genaiqna.service.vector.VectorStoreService;
import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class QdrantVectorStoreService implements VectorStoreService {
    private static final String QDRANT_URL = "http://localhost:6333";
    private static final String COLLECTION = "documents";

    private final OkHttpClient client = new OkHttpClient();

    @PostConstruct
    public void init() throws IOException {
        createCollectionIfNotExists();
    }

    private void createCollectionIfNotExists() throws IOException {
        String body = """
                {
                  "vectors": {
                    "size": 768,
                    "distance": "Cosine"
                  }
                }
                """;

        Request request = new Request.Builder()
                .url(QDRANT_URL + "/collections/" + COLLECTION)
                .put(RequestBody.create(
                        body,
                        MediaType.parse("application/json")))
                .build();

        client.newCall(request).execute().close();
    }

    @Override
    public void upsert(String id, float[] vector, Map<String, Object> payload) {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> point = Map.of(
                "id", id,
                "vector", vector,
                "payload", payload
        );

        Map<String, Object> bodyMap = Map.of(
                "points", List.of(point)
        );

        try {
            String body = mapper.writeValueAsString(bodyMap);

            Request request = new Request.Builder()
                    .url(QDRANT_URL + "/collections/" + COLLECTION + "/points?wait=true")
                    .put(RequestBody.create(
                            body,
                            MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException(
                            "Upsert failed: " + response.body().string()
                    );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Map<String, Object>> search(float[] queryVector, int topK) {
        // implemented below
        String body = """
                {
                  "vector": %s,
                  "limit": %d,
                  "with_payload": true
                }
                """.formatted(Arrays.toString(queryVector), topK);

        Request request = new Request.Builder()
                .url(QDRANT_URL + "/collections/" + COLLECTION + "/points/search")
                .post(RequestBody.create(
                        body,
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            return List.of(Map.of("raw", json));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
