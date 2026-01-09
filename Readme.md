# GenAI Knowledge Assistant Backend

A backend system designed to ingest documents, generate embeddings, store them in a vector database, and enable semantic
retrieval for LLM-based question answering.

This project is built incrementally with a strong focus on **backend engineering discipline**, **GenAI fundamentals**,
and **FAANG-style system design**.

---

## 🎯 Project Goals

- Design clean, scalable REST APIs for GenAI workflows
- Treat LLMs as deterministic backend dependencies
- Implement semantic search using embeddings and a vector database
- Build the foundation for Retrieval-Augmented Generation (RAG)

---

## 🧱 Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3
- **HTTP Client:** OkHttp
- **LLM (Dev):** Groq / Ollama (provider-agnostic)
- **Embeddings:** `nomic-embed-text`
- **Vector Database:** Qdrant (Docker, local)
- **Build Tool:** Maven
- **Version Control:** Git

---

## 🚀 APIs Implemented

### Health & Infra

- `GET /health`  
  → Service health check

### LLM Integration

- `GET /llm/test`  
  → Raw LLM response
- `GET /llm/structured-test`  
  → Deterministic JSON output using system prompts

### Embeddings

- `POST /embed`  
  → Converts input text into a high-dimensional embedding vector

### Vector DB (Testing)

- `GET /vector-test/run`  
  → Manual verification of vector insert + similarity search

---

## 🧠 Key Learnings (Critical for Interviews)

### Week 1 – GenAI & Backend Foundations

- Designed REST APIs with async GenAI workflows
- Integrated LLMs via raw HTTP calls (not SDK magic)
- Controlled LLM behavior using:
    - System prompts
    - Temperature = 0
    - Structured JSON output
- Understood token usage, latency, and cost trade-offs
- Generated and analyzed real embedding vectors

### Day 8 – Vector Database & Semantic Search

- Set up **Qdrant** locally using Docker
- Learned that:
    - Vector DBs require strict ID formats (UUID / unsigned int)
    - Embeddings must be serialized as numeric arrays (not strings)
    - Writes can be async unless `wait=true` is specified
- Successfully:
    - Inserted embeddings into Qdrant
    - Retrieved semantically similar text using cosine similarity
- Verified semantic match using real examples:
    - "Spring Boot is a Java framework"
    - "Java backend framework"

---

## 🧪 Example Vector Search Result

```json
{
  "id": "160747ed-104b-4d67-8c63-1deff0aed9d9",
  "score": 0.756,
  "payload": {
    "text": "Spring Boot is a Java framework"
  }
}
