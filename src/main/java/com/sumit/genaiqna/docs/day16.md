## 🎯 Objective of Day 16

The goal of Day 16 was to protect the GenAI backend from **abuse and overload** by implementing **rate limiting**.

Specifically, we added:

* Per-API-key request limits
* Deterministic request blocking after threshold
* Clear error signaling (`429 Too Many Requests`)
* A clean implementation with a clear path to distributed scaling

This is a **critical backend hardening step**, especially for GenAI systems where each request can be expensive.

---

## 🧠 Why Rate Limiting Is Critical in GenAI Backends

GenAI systems are uniquely vulnerable because:

* LLM calls are **slow and costly**
* Vector DB queries consume CPU and memory
* A single abusive client can exhaust system capacity

> Authentication answers **who** is calling.
> Rate limiting controls **how much** they can call.

---

## 🧠 Rate Limiting Strategy

### Chosen Strategy: **Fixed Window, In-Memory**

* **Scope:** Per API key
* **Limit:** 20 requests per minute
* **Window:** 60 seconds
* **Action on exceed:** Block request with HTTP `429`

This strategy is:

* Simple
* Deterministic
* Easy to reason about
* Interview-friendly

---

## 🧱 Implementation Overview

---

### 1️⃣ RateLimiter Component

📁 `security/RateLimiter.java`

```java

@Component
public class RateLimiter {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_MILLIS = 60_000;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public boolean allowRequest(String apiKey) {

        long now = Instant.now().toEpochMilli();

        Counter counter = counters.compute(apiKey, (key, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_MILLIS) {
                return new Counter(1, now);
            }
            existing.count++;
            return existing;
        });

        return counter.count <= MAX_REQUESTS;
    }

    private static class Counter {
        int count;
        long windowStart;

        Counter(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
```

### Why this works:

* Thread-safe (`ConcurrentHashMap`)
* O(1) operations per request
* No external dependencies
* Predictable behavior

---

### 2️⃣ Integration with Authentication Filter

Rate limiting is applied **after API key validation**, ensuring fairness per client.

📁 `security/ApiKeyAuthFilter.java`

```java
if(!rateLimiter.allowRequest(apiKey)){
        response.

setStatus(429);
    response.

getWriter().

write("Too Many Requests");
    return;
            }
```

---

## 🧠 Request Flow with Rate Limiting

```
Incoming Request
   ↓
Public endpoint? → allow
   ↓
API key validation → reject if invalid
   ↓
Rate limiter check → reject if exceeded
   ↓
Controller execution
```

This ensures:

* Only authenticated users are rate limited
* Limits are applied fairly
* System resources are protected early

---

## 🧪 Verified Behavior

### ✅ Within Rate Limit

```bash
curl -H "X-API-KEY: my-secret-key-123" \
     -X POST http://localhost:8080/ask
```

✔ Request succeeds

---

### ❌ Exceed Rate Limit

After 20 requests within one minute:

```bash
HTTP/1.1 429 Too Many Requests
Too Many Requests
```

✔ Request blocked
✔ System protected

---

## 🔒 Which APIs Are Rate Limited?

### Rate Limited

* `/ask`
* `/search`
* `/documents/**`
* `/embed`

### Not Rate Limited

* `/health`
* `/swagger-ui/**`
* `/v3/api-docs/**`

This avoids blocking:

* Monitoring
* Documentation
* Developer tools

---

## 🧠 Key Learnings from Day 16

### 🔑 Rate Limiting Complements Authentication

* Auth identifies the caller
* Rate limiting controls usage
* Both are required for a secure backend

---

### 🔑 Simplicity First

* In-memory limits are sufficient for single-instance setups
* Complexity should be added only when scaling requires it

---

### 🔑 Deterministic Failure Is Good

* Clients get immediate feedback
* System remains stable under load
* Easier to debug and monitor

---

## 🎤 Interview Questions & Model Answers

### Q1: Why is rate limiting important in GenAI systems?

**Answer:**

> Because each request can trigger expensive LLM calls, uncontrolled traffic can quickly exhaust resources and increase
> costs.

---

### Q2: Why did you choose in-memory rate limiting?

**Answer:**

> The system currently runs as a single instance. In-memory rate limiting is simple, fast, and sufficient at this scale.

---

### Q3: What are the limitations of this approach?

**Answer:**

> It doesn’t work across multiple instances and resets on application restart.

---

### Q4: How would you scale this in production?

**Answer:**

> Use Redis with atomic counters or Lua scripts to enforce global rate limits across instances.

---

### Q5: Why fixed window instead of sliding window or token bucket?

**Answer:**

> Fixed window is simple and predictable. More advanced algorithms can be introduced if smoother traffic shaping is
> required.

---

### Q6: Why apply rate limiting after authentication?

**Answer:**

> Authentication identifies the caller, allowing fair per-client rate limiting.

---

## 🔮 Future Improvements (Not Implemented Yet)

* Redis-based distributed rate limiting
* Per-endpoint limits
* Adaptive or dynamic limits
* User-specific quotas
* Burst handling via token bucket algorithm

---

## 🏁 Day 16 Outcome

✅ Rate limiting implemented
✅ Requests blocked after threshold
✅ Clear error responses
✅ Clean integration with authentication
✅ Interview-ready explanation

---

## 📌 Status

**Day 16 – COMPLETE**

---