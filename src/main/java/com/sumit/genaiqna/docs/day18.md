# 🎯 Objective of Day 18

The goal of Day 18 was to convert the **document ingestion pipeline** from a **synchronous HTTP workflow** to an *
*asynchronous background processing pipeline**.

Instead of processing ingestion inside the request thread, the system now:

1. Accepts the document upload request
2. Places an ingestion job into a queue
3. Immediately returns a response
4. Processes the ingestion asynchronously in the background

This design improves **latency, scalability, and system resilience**.

---

# 🧠 Problem With Synchronous Ingestion

Before Day 18, the ingestion flow looked like this:

```
Client Request
   ↓
Upload Document
   ↓
Chunk Text
   ↓
Generate Embeddings
   ↓
Store Vectors
   ↓
Return Response
```

Problems:

* Request latency could reach **several seconds**
* HTTP threads remain **blocked**
* Multiple large documents could **exhaust thread pool**
* Poor scalability under load

---

# 🧠 Async Ingestion Architecture

The improved architecture decouples request handling from processing.

```
Client
   ↓
Upload API
   ↓
Queue Ingestion Job
   ↓
Return 202 Accepted

Background Worker
   ↓
Take Job From Queue
   ↓
Chunk Document
   ↓
Generate Embeddings
   ↓
Store Vectors
```

This pattern is widely used in **large-scale ingestion pipelines**.

---

# 🧱 Key Components Introduced

Three main components were added:

1. **Ingestion Job Model**
2. **Ingestion Queue**
3. **Background Worker**

---

# 🧱 1️⃣ Ingestion Job Model

Represents a unit of work.

```java
public record IngestionJob(
        UUID documentId,
        String content
) {
}
```

Why this is useful:

* Clear representation of processing tasks
* Decouples job submission from execution
* Easy to serialize if moving to distributed queues later

---

# 🧱 2️⃣ Ingestion Queue

An in-memory message queue stores ingestion jobs.

```java

@Component
public class IngestionQueue {

    private final BlockingQueue<IngestionJob> queue =
            new LinkedBlockingQueue<>();

    public void submit(IngestionJob job) {
        queue.offer(job);
    }

    public IngestionJob take() throws InterruptedException {
        return queue.take();
    }
}
```

Characteristics:

* Thread-safe
* Blocking retrieval
* Simple producer-consumer pattern

---

# 🧱 3️⃣ Background Worker

Consumes ingestion jobs and processes them asynchronously.

```java

@Component
public class IngestionWorker {

    private final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    @PostConstruct
    public void start() {
        executor.submit(() -> {
            while (true) {
                IngestionJob job = queue.take();
                ingestionService.process(job);
            }
        });
    }
}
```

Features:

* Dedicated worker threads
* Continuous job consumption
* Controlled concurrency

---

# 🧱 Controller Modification

The upload endpoint was modified to **enqueue jobs instead of processing them**.

### Before (Synchronous)

```
Upload → Process → Respond
```

### After (Async)

```
Upload → Enqueue → Respond
```

Implementation:

```java

@PostMapping("/ingest")
public ResponseEntity<Map<String, Object>> ingest(
        @RequestBody Map<String, String> body
) {

    UUID documentId = UUID.randomUUID();

    ingestionQueue.submit(
            new IngestionJob(documentId, body.get("content"))
    );

    return ResponseEntity.accepted().body(
            Map.of(
                    "documentId", documentId,
                    "status", "INGESTION_STARTED"
            )
    );
}
```

---

# 🧪 Verified Behavior

### Client Response

```
POST /documents/ingest
```

Response:

```json
{
  "status": "INGESTION_STARTED",
  "documentId": "82649903-3d28-4f9e-b17d-62ed93fa48cf"
}
```

HTTP status:

```
202 Accepted
```

---

### Background Processing Logs

```
Processing ingestion job for doc=82649903-3d28-4f9e-b17d-62ed93fa48cf
Completed ingestion for doc=82649903-3d28-4f9e-b17d-62ed93fa48cf
```

This confirms:

* Job queued successfully
* Worker thread processed the job
* Pipeline executed asynchronously

---

# 🧠 Advantages of Async Ingestion

### 1️⃣ Faster API Responses

Upload endpoint returns immediately.

---

### 2️⃣ Better Thread Utilization

HTTP threads are freed quickly.

---

### 3️⃣ Improved Scalability

Multiple worker threads process ingestion concurrently.

---

### 4️⃣ System Decoupling

Request handling is separated from processing.

---

# ⚠️ Limitations of Current Approach

The current queue is **in-memory**, meaning:

* Jobs are lost if the application crashes
* Only one instance can process jobs

---

# 🔮 Production Improvements

In a real production system, the queue would be replaced with:

| Technology     | Purpose                   |
|----------------|---------------------------|
| Kafka          | High-throughput streaming |
| RabbitMQ       | Reliable messaging        |
| AWS SQS        | Managed queue             |
| Google Pub/Sub | Distributed event system  |

These systems provide:

* Persistence
* Retry support
* Dead-letter queues
* Horizontal scalability

---

# 🎤 Interview Questions & Answers

### Q1: Why move ingestion to asynchronous processing?

Answer:

> Embedding generation and vector storage are expensive operations. Processing them synchronously blocks HTTP threads
> and increases request latency. Async processing decouples request handling from heavy computation.

---

### Q2: Why use a queue instead of directly spawning threads?

Answer:

> Queues provide controlled backpressure and decouple producers from consumers. This ensures the system remains stable
> under heavy load.

---

### Q3: Why use `ExecutorService`?

Answer:

> It allows controlled concurrency with a fixed thread pool, preventing unbounded thread creation.

---

### Q4: What happens if the application crashes?

Answer:

> Since the queue is in-memory, pending jobs are lost. In production systems, a durable message queue such as Kafka or
> SQS would be used.

---

### Q5: How would you scale this ingestion pipeline?

Answer:

> Replace the in-memory queue with a distributed queue and run multiple ingestion workers across instances.

---

# 🏁 Day 18 Outcome

The system now supports:

* Asynchronous document ingestion
* Queue-based processing
* Background worker execution
* Immediate API responses
* Improved scalability

This marks the transition from **simple API processing** to a **real ingestion pipeline architecture**.

---

# 📌 Status

**Day 18 – COMPLETE**

---