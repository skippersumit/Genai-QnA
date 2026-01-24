## 🧠 Why Performance Measurement Matters in GenAI Systems

In GenAI backends:

* Latency directly affects user experience
* LLM calls dominate cost
* Retrieval must be fast enough to not become a bottleneck
* Optimizations must be data-driven

> You cannot optimize what you do not measure.

---

## 🧠 Latency Breakdown Model

The `/ask` API latency can be decomposed as:

```
Total /ask latency =
  Query embedding latency
+ Vector database search latency
+ Prompt construction
+ LLM generation latency
```

Day 13 focuses on **measuring each component individually**.

---

## 🧱 Instrumentation Strategy

A simple in-code stopwatch was used to avoid introducing external dependencies.

```java
public class Stopwatch {

    private final long start;

    private Stopwatch() {
        this.start = System.currentTimeMillis();
    }

    public static Stopwatch start() {
        return new Stopwatch();
    }

    public long elapsedMillis() {
        return System.currentTimeMillis() - start;
    }
}
```

This approach:

* Keeps overhead minimal
* Works in local and production environments
* Is easy to remove or replace later

---

## 📊 Measured Benchmarks (Local)

All benchmarks were measured on a **local development machine**, using:

* Ollama (local inference)
* Qdrant (Docker)
* Small dataset (no indexing yet)

---

### 1️⃣ Embedding Latency

**What was measured:**

* Time to generate embeddings for a query
* Includes model inference + JSON parsing

**Observed log:**

```
Embedding latency: 41 ms
```

**Interpretation:**

* Embedding generation is relatively fast
* Suitable for real-time queries
* Ingestion throughput will be bounded by this step

---

### 2️⃣ Vector Search Latency

**What was measured:**

* End-to-end vector search time in Qdrant
* Includes network + serialization + similarity computation

**Observed log:**

```
SearchService latency (query+vector search): 12 ms
```

**Interpretation:**

* Vector search is extremely fast
* Even without HNSW indexing, brute-force search is acceptable for small datasets
* Confirms retrieval is not the system bottleneck

---

### 3️⃣ End-to-End `/ask` Latency

**What was measured:**

* Full request lifecycle:

    * Query embedding
    * Vector search
    * Prompt construction
    * LLM generation

**Observed log:**

```
Total /ask latency: 374 ms
```

**Interpretation:**

* Well within acceptable limits for interactive systems
* Dominated by LLM generation time
* Retrieval overhead is negligible compared to LLM latency

---

## 📈 Summary Table

| Component               | Latency     |
|-------------------------|-------------|
| Embedding               | ~41 ms      |
| Vector search           | ~12 ms      |
| LLM generation + prompt | ~300+ ms    |
| **Total `/ask`**        | **~374 ms** |

---

## 🧠 Key Learnings from Day 13

### 🔑 LLM Is the Bottleneck

* Retrieval is cheap
* LLM inference dominates latency and cost
* Optimizing retrieval yields diminishing returns compared to LLM optimizations

---

### 🔑 Retrieval Scales Better Than Generation

* Vector DB can handle high QPS
* LLM calls scale poorly without batching or caching
* Reinforces the need for caching strategies later

---

### 🔑 Measuring Early Prevents Bad Design

* Without numbers, teams over-optimize the wrong components
* Performance baselines guide future architectural decisions

---

## 🎤 Interview Questions & Model Answers

### Q1: What is the biggest latency bottleneck in a RAG system?

**Answer:**

> LLM generation dominates latency; embedding and vector search are comparatively fast.

---

### Q2: Why measure embedding latency separately?

**Answer:**

> Embedding latency directly impacts ingestion throughput and query responsiveness, especially for systems that embed
> queries in real time.

---

### Q3: Why is vector search so fast?

**Answer:**

> Vector databases are optimized for similarity search, and for small datasets brute-force search is often sufficient.
> Indexing mainly improves performance at scale.

---

### Q4: How would you reduce `/ask` latency?

**Answer:**

> Cache frequent query embeddings, cache popular answers, reduce top-K, use faster or smaller models, and batch LLM
> calls where possible.

---

### Q5: Why didn’t you optimize anything yet?

**Answer:**

> Premature optimization is risky. Establishing a baseline allows informed, data-driven optimizations later.

---