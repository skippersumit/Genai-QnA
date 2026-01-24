## Day 11 – RAG Implementation (Ask API)

---

## 🎯 Objective of Day 11

The goal of Day 11 was to implement **Retrieval-Augmented Generation (RAG)** by combining:

* User query
* Retrieved document chunks (Day 10)
* Prompt construction
* LLM generation

The final output must be a **grounded answer**, derived **only** from retrieved documents.

---

## 🧠 High-Level RAG Flow

```
User Query
   ↓
Query Embedding
   ↓
Vector DB (Similarity Search)
   ↓
Top-K Relevant Chunks
   ↓
Prompt Construction (Context + Question)
   ↓
LLM
   ↓
Grounded Answer + Sources
```

This completes the **read + generate path** of the GenAI system.

---

## 📌 API Design

### Endpoint

```
POST /ask
```

### Input

```json
{
  "query": "What is Spring Boot?",
  "topK": 2
}
```

### Output

```json
{
  "answer": "Spring Boot is a Java framework used for building backend services.",
  "sources": [
    {
      "documentId": "7d79ec59-1e92-41c5-a28f-18f9330d856b",
      "chunkIndex": 0
    }
  ]
}
```

---

## 🧱 Core Components

### 1️⃣ AskController – API Entry Point

```java

@PostMapping
public ResponseEntity<Map<String, Object>> ask(
        @RequestBody Map<String, Object> body
) {
    String query = (String) body.get("query");
    int topK = body.get("topK") == null ? 3 : (int) body.get("topK");

    return ResponseEntity.ok(
            askService.ask(query, topK)
    );
}
```

**Responsibility:**

* Accept user input
* Delegate to RAG service
* Return final response

No business logic lives here.

---

### 2️⃣ AskService – RAG Orchestration

```java
public Map<String, Object> ask(String query, int topK) {

    // Step 1: Retrieve relevant chunks
    List<Map<String, Object>> chunks =
            searchService.search(query, topK);

    // Step 2: Build grounded prompt
    String prompt = buildPrompt(query, chunks);

    // Step 3: Generate answer via LLM
    String answer = llmService.generate(prompt);

    // Step 4: Attach sources
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
```

This service coordinates **retrieval + generation**.

---

## 🧠 Prompt Design (Grounding)

```text
You are a backend QA assistant.
Answer the question ONLY using the context below.
If the answer is not present in the context, say "I don't know".

Context:
- <retrieved chunk 1>
- <retrieved chunk 2>

Question:
<user query>
```

### Why this prompt works

* Explicit grounding instruction
* No permission to hallucinate
* Clear fallback behavior

This is the **first layer of hallucination control**.

---

## 🚨 Major Problem Faced (Critical Learning)

### ❌ Problem: Raw LLM Response Returned

Initial `/ask` response looked like this:

```json
{
  "answer": "{ \"id\": \"chatcmpl-...\", \"choices\": [...] }"
}
```

This meant:

* The backend returned the **entire LLM API response**
* Instead of the actual answer

---

## 🧠 Why This Happened

LLM providers (Groq / OpenAI / Ollama) return **metadata-heavy responses**:

* IDs
* Token usage
* Model info
* Nested message content

Returning this directly violates **backend API design principles**.

---

## ✅ Correct Fix (Important)

The fix was applied in the **LLM integration layer**, not in:

* Controller ❌
* AskService ❌

### 📁 `LlmService.generate()`

```java
public String generate(String prompt) {

    String rawResponse = callLlmApi(prompt);

    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(rawResponse);

        // Extract message content
        String content =
                root.get("choices")
                        .get(0)
                        .get("message")
                        .get("content")
                        .asText();

        // Parse structured JSON returned by LLM
        JsonNode structured = mapper.readTree(content);

        return structured.get("answer").asText();

    } catch (Exception e) {
        throw new RuntimeException("Failed to parse LLM response", e);
    }
}
```

---

## 🧠 Why This Fix Is Architecturally Correct

> LLMs are **dependencies**, not API contracts.

Just like:

* JDBC result sets
* External REST APIs

The backend must:

* Normalize responses
* Expose a stable API to clients
* Shield consumers from provider changes

This is **clean architecture**.

---

## 🧪 Final Verified Output

```json
{
  "answer": "Spring Boot is a Java framework used for building backend services.",
  "sources": [
    {
      "documentId": "7d79ec59-1e92-41c5-a28f-18f9330d856b",
      "chunkIndex": 0
    }
  ]
}
```

This confirms:

* Retrieval worked
* Prompt grounding worked
* LLM answered correctly
* Backend returned a clean response

---

## 🎤 Interview Questions & Answers (VERY IMPORTANT)

### Q1: What is RAG?

**Answer:**

> Retrieval-Augmented Generation combines document retrieval with LLM generation to ground answers in external
> knowledge.

---

### Q2: Why not let the LLM answer directly?

**Answer:**

> Direct generation can hallucinate. Retrieval constrains the LLM to known, verifiable context.

---

### Q3: How do you prevent hallucinations?

**Answer:**

> By injecting retrieved context into the prompt and explicitly instructing the model to answer only from that context.

---

### Q4: Why parse LLM responses instead of returning them?

**Answer:**

> LLM responses contain provider-specific metadata. Normalizing responses ensures API stability and clean contracts.

---

### Q5: What happens if the answer is not in the documents?

**Answer:**

> The prompt instructs the model to respond with “I don’t know,” which avoids hallucination.

---