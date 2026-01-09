## Day 8 – Vector Database Setup & Semantic Search

---

## 🎯 Objective of Day 8

The goal of Day 8 was to build a **real vector database integration** and prove that:

* Text can be converted into embeddings
* Embeddings can be stored in a vector database
* Semantically similar text can be retrieved using vector similarity

This forms the **core foundation of Retrieval-Augmented Generation (RAG)** systems.

---

## 🧠 What Is a Vector (Practical Understanding)

An **embedding vector** is:

* A high-dimensional numeric representation of text
* Each dimension captures a latent semantic feature
* Similar meanings → vectors close together in vector space

Key properties:

* Vectors are **not human-readable**
* They are only useful for **mathematical comparison**
* Distance metrics (cosine similarity) determine semantic closeness

---

## 🧠 What Vectors Are NOT

* ❌ Not keywords
* ❌ Not summaries
* ❌ Not token IDs
* ❌ Not interpretable individually

The **entire vector together** represents meaning.

---

## 🧠 Why Backend Engineers Care About Vectors

Vectors enable:

* Semantic search
* Document retrieval
* Deduplication
* RAG pipelines
* Knowledge grounding for LLMs

Traditional SQL databases cannot efficiently perform **high-dimensional similarity search**, which is why **vector
databases exist**.

---

## 🧱 Vector Database Choice

### Chosen: **Qdrant (Local via Docker)**

Reasons:

* Production-grade vector DB
* Simple HTTP API
* Supports cosine similarity
* Accepted and understood in interviews
* Easy local development

### Run Qdrant

```bash
docker run -d \
  --name qdrant \
  -p 6333:6333 \
  qdrant/qdrant
```

---

## 🧱 Vector Store Contract (Abstraction)

```java
public interface VectorStoreService {

    void upsert(
            String id,
            float[] vector,
            Map<String, Object> payload
    );

    List<Map<String, Object>> search(
            float[] queryVector,
            int topK
    );
}
```

**Why this design:**

* Provider-agnostic
* Payload allows metadata storage (text, docId, chunkId later)
* Clean separation between backend logic and vector DB

---

## 🧱 Qdrant Implementation

### Collection Creation

```java

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
            .url(QDRANT_URL + "/collections/documents")
            .put(RequestBody.create(
                    body,
                    MediaType.parse("application/json")))
            .build();

    client.newCall(request).execute().close();
}
```

**Key Learnings:**

* Vector size **must match embedding model**
* For `nomic-embed-text`, size = **768**
* Cosine similarity is ideal for text embeddings

---

## 🧱 Inserting Vectors (Upsert)

```java

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
                .url(QDRANT_URL + "/collections/documents/points?wait=true")
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
```

### Important Learnings

* ❌ **Stringified arrays do not work**
* ✅ Vectors must be serialized as **numeric JSON arrays**
* `wait=true` ensures vectors are searchable immediately
* Qdrant enforces **strict ID formats**

---

## 🧠 Qdrant Point ID Rules (Critical)

Qdrant point IDs must be:

* ✅ UUID
* ✅ Unsigned integer

❌ Arbitrary strings like `"doc_test_1"` are invalid.

Correct usage:

```java
String id = UUID.randomUUID().toString();
```

Metadata belongs in **payload**, not IDs.

---

## 🧱 Searching Vectors (Similarity Search)

```java

@Override
public List<Map<String, Object>> search(float[] queryVector, int topK) {

    String body = """
            {
              "vector": %s,
              "limit": %d,
              "with_payload": true
            }
            """.formatted(Arrays.toString(queryVector), topK);

    Request request = new Request.Builder()
            .url(QDRANT_URL + "/collections/documents/points/search")
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
```

### Why `with_payload` Matters

Without it:

* Qdrant returns IDs only
* Results appear empty or useless

---

## 🧪 Manual End-to-End Verification

### Insert Text

```text
"Spring Boot is a Java framework"
```

### Search Query

```text
"Java backend framework"
```

### Result

```json
{
  "id": "160747ed-104b-4d67-8c63-1deff0aed9d9",
  "score": 0.756,
  "payload": {
    "text": "Spring Boot is a Java framework"
  }
}
```

**Interpretation:**

* Score > 0.7 → strong semantic match
* Confirms embeddings + vector DB are working correctly

---

## 🧠 Key Interview Takeaways from Day 8

You can now confidently say:

* Vector DBs require strict data formats
* Embeddings must come from the same model to be comparable
* Cosine similarity measures semantic direction, not magnitude
* IDs should be opaque (UUID), metadata goes in payload
* Vector DB writes can be async unless explicitly awaited

---

## 🔜 What This Enables Next

With Day 8 complete, the system is ready for:

* Document chunking
* Embedding per chunk
* Top-K retrieval
* Retrieval-Augmented Generation (RAG)

---

## ✅ Day 8 Status

* Vector DB setup: ✅
* Insert embeddings: ✅
* Similarity search: ✅
* Semantic match verified: ✅

---