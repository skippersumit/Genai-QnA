# GenAI Knowledge Assistant Backend

## Project Goal

A backend system that ingests documents, embeds them, and answers user queries using LLMs with retrieval-based
grounding.

## Tech Stack

- Java 17
- Spring Boot 3
- LLM: Groq / Ollama (pluggable)
- Embeddings: nomic-embed-text
- HTTP Client: OkHttp

## APIs Implemented (Week 1)

- GET /health
- GET /llm/test
- GET /llm/structured-test
- POST /embed

## Week 1 Learnings

- Designed REST APIs with async GenAI workflows
- Integrated LLMs as backend dependencies
- Controlled LLM output using system prompts
- Generated semantic embeddings from text
- Understood cost, latency, and determinism trade-offs

## Next Steps

- Vector database integration
- Document ingestion pipeline
- Retrieval-Augmented Generation (RAG)
