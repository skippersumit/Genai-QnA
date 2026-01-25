## 🧠 Why Authentication Matters in GenAI Systems

GenAI backends are **expensive and sensitive**:

* LLM calls cost money
* Vector DB queries consume resources
* Without auth, anyone can abuse the system

> Even internal tools must assume hostile traffic.

Authentication is the **first line of defense**.

---

## 🔐 Authentication Strategy Chosen

### ✅ API Key Authentication

**How it works:**

* Client sends an API key in request headers
* Backend validates the key
* Requests without a valid key are rejected

### Request Header

```
X-API-KEY: my-secret-key-123
```

---

## 🤔 Why API Key (and Not JWT)?

### Reasons:

* No user identity or login flow yet
* JWT adds unnecessary complexity at this stage
* API keys are:

    * Simple
    * Widely used in backend services
    * Easy to rotate and manage

> API keys secure **access**, not **identity** — which is exactly what we need right now.

---

## 🧱 Implementation Details

---

### 1️⃣ API Key Configuration

📁 `application.yml`

```yaml
security:
  api-key: my-secret-key-123
```

In production, this would be injected via:

* Environment variables
* Secret manager (Vault / AWS Secrets Manager)

---

### 2️⃣ Authentication Filter

📁 `security/ApiKeyAuthFilter.java`

```java

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${security.api-key}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Allow public endpoints
        if (path.startsWith("/health") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
```

### Responsibilities:

* Intercepts every request
* Validates API key
* Rejects unauthorized access early
* Keeps controllers clean

---

### 3️⃣ Security Configuration

📁 `security/SecurityConfig.java`

```java

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
```

**Why this approach:**

* Avoids full Spring Security complexity
* Manual control via filter
* Clear, predictable behavior

---

## 🧪 Verified Behavior (Manual Tests)

---

### ❌ Request Without API Key

```bash
curl http://localhost:8080/ask
```

**Response**

```
401 Unauthorized
```

✔ Correctly blocked

---

### ✅ Request With API Key

```bash
curl -H "X-API-KEY: my-secret-key-123" \
     -X POST http://localhost:8080/ask \
     -H "Content-Type: application/json" \
     -d '{"query":"What is Spring Boot?"}'
```

**Response**

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

✔ Authorized
✔ Functional
✔ Secure

---

## 🔒 Secured vs Public Endpoints

### 🔐 Secured (Require API Key)

* `/documents/**`
* `/search`
* `/ask`
* `/embed`

### 🌐 Public

* `/health`
* `/swagger-ui/**`
* `/v3/api-docs/**`

This balance supports:

* Developer usability
* Production security

---

## 🧠 Key Learnings from Day 15

### 🔑 Authentication ≠ Identity

* API keys secure access
* JWTs secure identity
* Choose based on system needs

---

### 🔑 Filters Are Powerful

* Centralized enforcement
* No controller pollution
* Easy to extend later

---

### 🔑 Security Is Incremental

* Start simple
* Harden progressively
* Don’t overbuild prematurely

---

## 🎤 Interview Questions & Answers

### Q1: Why did you choose API key authentication?

**Answer:**

> The system currently doesn’t manage user identity. API keys are ideal for service-level authentication and are simpler
> and more appropriate at this stage.

---

### Q2: What are the limitations of API key auth?

**Answer:**

> API keys don’t represent users, can’t be easily revoked per user, and are less secure than short-lived tokens like
> JWTs.

---

### Q3: How would you rotate API keys?

**Answer:**

> Store multiple valid keys, gradually rotate clients to new keys, then revoke old ones.

---

### Q4: How would you upgrade this to JWT?

**Answer:**

> Introduce authentication endpoints, issue JWTs on login, replace the API key filter with JWT validation.

---

### Q5: Why not rely entirely on Spring Security?

**Answer:**

> Manual filters provide clearer control and avoid unnecessary complexity for simple authentication needs.

---