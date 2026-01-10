## Day 10 – Similarity Search (Retrieval Core)

---

## 🎯 Objective of Day 10

The goal of Day 10 was to implement the **retrieval layer** of a GenAI system.

By the end of Day 10, the system is capable of:

* Accepting a user query
* Converting the query into an embedding
* Searching a vector database using similarity search
* Returning the **top-K most relevant text chunks**

This completes the **read path** of the system and forms the core of **Retrieval-Augmented Generation (RAG)**.

---

## 🧠 High-Level Retrieval Flow

```
User Query
   ↓
Query Embedding
   ↓
Vector Database (Qdrant)
   ↓
Top-K Relevant Chunks
```

No LLM is involved yet.
The output is **retrieved context**, not an answer.

---

## 📌 API Design

### Endpoint

```
POST /search
```

---

### Input

```json
{
  "query": "Java backend framework",
  "topK": 2
}
```

**Field Explanation:**

* `query` → Natural language user query
* `topK` → Number of most similar chunks to retrieve (default = 3)

---

### Output

```json
{
  "results": [
    {
      "score": 0.79313195,
      "text": "Spring Boot is a Java framework used for building backend services...",
      "documentId": "7d79ec59-1e92-41c5-a28f-18f9330d856b",
      "chunkIndex": 0
    }
  ]
}
```

**Field Explanation:**

* `score` → Cosine similarity score (higher = more relevant)
* `text` → Retrieved document chunk
* `documentId` → ID of the source document
* `chunkIndex` → Position of the chunk within the document

---

## 🧱 Core Components

### 1️⃣ Controller – Search Entry Point

```java

@PostMapping
public ResponseEntity<Map<String, Object>> search(
        @RequestBody Map<String, Object> body
) {
    String query = (String) body.get("query");
    int topK = body.get("topK") == null ? 3 : (int) body.get("topK");

    return ResponseEntity.ok(
            Map.of("results", searchService.search(query, topK))
    );
}
```

**Responsibilities:**

* Accept search request
* Handle default `topK`
* Delegate retrieval logic to service layer

No business logic is embedded here.

---

### 2️⃣ Service – Retrieval Logic

```java
public List<Map<String, Object>> search(String query, int topK) {

    // Generate embedding for query
    String embeddingJson = embeddingService.embed(query);
    float[] queryVector = EmbeddingParser.extractVector(embeddingJson);

    // Perform similarity search
    List<Map<String, Object>> rawResults =
            vectorStoreService.search(queryVector, topK);

    // Parse and return relevant chunks
    return extractChunks(rawResults);
}
```

**Responsibilities:**

* Convert user query into an embedding
* Query the vector database
* Transform raw DB response into API-friendly format

---

### 3️⃣ Parsing Qdrant Results

```java
private List<Map<String, Object>> extractChunks(
        List<Map<String, Object>> rawResults
) {
    String json = (String) rawResults.get(0).get("raw");
    JsonNode root = mapper.readTree(json);
    JsonNode resultArray = root.get("result");

    List<Map<String, Object>> chunks = new ArrayList<>();

    for (JsonNode node : resultArray) {
        chunks.add(
                Map.of(
                        "score", node.get("score").asDouble(),
                        "text", node.get("payload").get("text").asText(),
                        "documentId", node.get("payload").get("documentId").asText(),
                        "chunkIndex", node.get("payload").get("chunkIndex").asInt()
                )
        );
    }
    return chunks;
}
```

**Why explicit parsing matters:**

* Makes response structure clear
* Avoids hiding complexity behind SDKs
* Demonstrates understanding of vector DB internals

---

## 🧠 Why Similarity Search Works

* Both documents and queries are embedded using the **same embedding model**
* Embeddings live in the **same vector space**
* Cosine similarity measures **semantic closeness**, not keyword overlap

This allows:

* “Java backend framework”
* to match
* “Spring Boot is a Java framework used for building backend services”

Even without shared keywords.

---

## 🧪 Verification

### Input Query

```text
"Java backend framework"
```

### Retrieved Chunk

```text
"Spring Boot is a Java framework used for building backend services..."
```

### Similarity Score

```text
0.79
```

**Interpretation:**

* Score > 0.7 indicates strong semantic relevance
* Confirms ingestion + retrieval pipeline is correct

---

## 🧠 Important Observations

### 1️⃣ Indexing vs Search

Even though `indexed_vectors_count` may be `0`, retrieval still works.

Reason:

* Qdrant performs brute-force search until indexing threshold is reached
* Indexing is a performance optimization, not a correctness requirement

---

### 2️⃣ Top-K Retrieval

Retrieving multiple chunks:

* Improves recall
* Allows better context selection downstream
* Is essential for effective RAG

---

## 🎤 Interview Talking Points (Very Important)

You should be able to explain:

### Why embed queries?

> Similarity search requires both documents and queries to be represented in the same vector space.

### Why retrieve chunks instead of full documents?

> Smaller chunks provide more focused and relevant context to LLMs.

### Why top-K instead of top-1?

> To balance recall and precision and give the LLM multiple grounding options.

### Why retrieval before generation?

> Retrieval grounds the LLM in factual context and reduces hallucinations.

### Why no LLM yet?

> Separation of concerns: retrieval and generation are independent stages.

---

## 🏁 Day 10 Outcome

✅ Query embedding implemented
✅ Vector DB similarity search working
✅ Relevant chunks retrieved
✅ Retrieval core completed

This completes the **RAG retrieval layer**.

---

## 🔜 What This Enables Next

With Day 10 complete, the system is ready for:

* Prompt construction with retrieved chunks
* Retrieval-Augmented Generation
* Answer grounding and hallucination control

---