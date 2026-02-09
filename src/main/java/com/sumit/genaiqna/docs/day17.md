## Day 17 – Caching Strategy (Redis Integration)

---

## 🎯 Objective of Day 17

The goal of Day 17 was to **reduce latency and cost** in the GenAI backend by introducing **Redis-based caching**.

Specifically, we implemented caching for:

* **Embeddings** (to avoid repeated model inference)
* **Frequent `/ask` responses** (to avoid repeated LLM calls)

This transforms the system from *functional* to *production-efficient*.

---

## 🧠 Why Caching Is Critical in GenAI Systems

GenAI workloads are expensive because:

* Embedding generation invokes ML models
* LLM calls dominate latency and cost
* Users frequently repeat similar queries

> Caching is the most effective and cheapest optimization in GenAI backends.

---

## 🧠 What We Chose to Cache (and Why)

### 1️⃣ Embeddings Cache

* **Key:** raw text
* **Value:** embedding vector
* **Why:**

    * Deterministic output
    * High compute cost
    * Used in both ingestion and querying

---

### 2️⃣ `/ask` Response Cache

* **Key:** `query + topK`
* **Value:** final answer + sources
* **Why:**

    * LLM calls are slow and expensive
    * High likelihood of repeated queries
    * Safe if inputs are identical

---

## ❌ What We Explicitly Did NOT Cache

* Vector DB search results
* Partial prompt states
* Ingestion pipeline outputs

**Reason:**

* High cache invalidation complexity
* Data changes frequently
* Risk of serving stale or incorrect data

This design choice is **intentional and interview-safe**.

---

## 🧱 Implementation Overview

---

### 1️⃣ Redis Setup

Redis was added as a separate service:

```yaml
services:
  redis:
    image: redis:7
    ports:
      - "6379:6379"
```

Redis acts as:

* A shared, fast in-memory store
* A cache across requests
* A future-ready component for distributed setups

---

### 2️⃣ Spring Cache Enablement

```java

@EnableCaching
@SpringBootApplication
public class GenaiQnaApplication {
}
```

Spring’s cache abstraction allows:

* Declarative caching
* Minimal code changes
* Easy cache backend replacement

---

### 3️⃣ Redis Configuration

```java

@Bean
public RedisTemplate<String, Object> redisTemplate(
        RedisConnectionFactory factory
) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(
            new GenericJackson2JsonRedisSerializer()
    );
    return template;
}
```

---

## 🧱 Embedding Cache Implementation

📁 `EmbeddingService.java`

```java

@Cacheable(value = "embeddings", key = "#text")
public float[] embed(String text) {

    log.info("Embedding cache MISS for text hash={}", text.hashCode());

    Stopwatch sw = Stopwatch.start();
    float[] vector = callOllamaEmbedding(text);

    log.info("Embedding computed in {} ms", sw.elapsedMillis());
    return vector;
}
```

### Behavior

* First request → Ollama inference
* Subsequent requests → Redis hit
* Transparent to callers

---

## 🧱 `/ask` Response Cache Implementation

📁 `AskService.java`

```java

@Cacheable(value = "askResponses", key = "#query + ':' + #topK")
public Map<String, Object> ask(String query, int topK) {

    log.info("CACHE MISS → executing /ask for query='{}'", query);

    Stopwatch total = Stopwatch.start();

    List<Map<String, Object>> chunks =
            searchService.search(query, topK);

    String prompt = PromptBuilder.buildGroundedPrompt(query, chunks);

    Map<String, Object> llmResult =
            llmService.generateGroundedAnswer(prompt);

    Map<String, Object> response =
            formatResponse(llmResult, chunks);

    log.info("Total /ask latency: {} ms", total.elapsedMillis());
    return response;
}
```

---

## 🧪 Verified Runtime Behavior

### First Request (Cache MISS)

```
CACHE MISS → executing /ask
Embedding cache MISS
Embedding computed in 408 ms
Vector search latency: 21 ms
Total /ask latency: 831 ms
```

---

### Second Request (Cache HIT)

```
CACHE HIT for /ask
```

* No embedding call
* No vector search
* No LLM call
* Response returned almost instantly

---

## 📈 Performance Impact Summary

| Scenario             | Latency   |
|----------------------|-----------|
| Cold `/ask`          | ~830 ms   |
| Warm `/ask` (cached) | ~10–20 ms |

> **~40–80× latency reduction** for repeated queries.

---

## 🧠 Key Learnings from Day 17

### 🔑 Cache the Expensive, Not the Cheap

* LLM calls dominate cost
* Embeddings are expensive but deterministic
* Retrieval is already fast

---

### 🔑 Cache Invalidation Is Hard

* Avoid caching data tied to mutable state
* Prefer caching final outputs

---

### 🔑 Caching Must Be Transparent

* Business logic unchanged
* Cache failure should degrade gracefully

---

## 🎤 Interview Questions & Answers

### Q1: Why cache embeddings?

**Answer:**

> Embeddings are deterministic and expensive to compute. Caching avoids repeated model inference and significantly
> improves performance.

---

### Q2: Why cache `/ask` responses?

**Answer:**

> LLM calls dominate latency and cost. Repeated queries benefit massively from caching the final answer.

---

### Q3: Why not cache vector search results?

**Answer:**

> Vector search depends on corpus state. Cache invalidation would be complex and error-prone.

---

### Q4: What happens if Redis goes down?

**Answer:**

> Cache misses occur, but the system continues to function correctly, just with higher latency.

---

### Q5: How would you invalidate cache on document updates?

**Answer:**

> Use versioned cache keys or namespace cache entries per document ingestion cycle.

---

### Q6: How does this scale in production?

**Answer:**

> Redis provides a shared cache across instances, enabling horizontal scaling.

---

## 🔮 Future Improvements (Not Implemented Yet)

* TTL-based expiration
* Cache eviction policies
* Query normalization
* Cache warm-up strategies
* Per-user cache scoping

---