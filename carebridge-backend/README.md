# CareBridge Backend

Java 17 / Spring Boot 3 backend for event-driven appointment rescheduling.

## Run

Requirements: JDK 17 and Maven 3.9+.

```bash
mvn spring-boot:run
```

Default scheduling strategy is rule-based; no API key is required.

## URLs

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:carebridgedb`
- User: `sa`; password blank

## Example requests

Create appointment:

```bash
curl -X POST http://localhost:8080/appointments -H "Content-Type: application/json" -d '{"patientName":"Taylor Smith","type":"ROUTINE","providerId":"PRV-004","slotId":"SLT-005"}'
```

Provider offline (automatically triggers asynchronous rescheduling):

```bash
curl -X PATCH http://localhost:8080/providers/PRV-001/status -H "Content-Type: application/json" -d '{"status":"OFFLINE"}'
```

View pending suggestions:

```bash
curl http://localhost:8080/suggestions?status=PENDING
```

Accept suggestion:

```bash
curl -X PATCH http://localhost:8080/suggestions/1 -H "Content-Type: application/json" -d '{"decision":"ACCEPTED"}'
```

Cancel a slot:

```bash
curl -X PATCH http://localhost:8080/slots/SLT-001/status -H "Content-Type: application/json" -d '{"status":"CANCELLED"}'
```

## AI mode

Set environment variables before startup:

```bash
export SCHEDULING_STRATEGY=ai
export OPENAI_API_KEY=your-key
mvn spring-boot:run
```

Use only a key for the configured provider/model. Do not commit credentials.

## Scope notes

The event processing uses in-process Spring events and asynchronous listeners. This is suitable for a local assessment
demonstration, but events can be lost if the process stops. Production hardening should use a transactional outbox and
durable message broker. H2 is in-memory and resets when the app restarts.

### Seed data

The H2 database loads the demo records from `src/main/resources/data.sql`: providers `PRV-001`–`PRV-005`, slots
`SLT-001`–`SLT-006`, and appointments `APT-001`–`APT-004`. Room IDs remain numeric because the current model uses
internal room records.

## LLM Gateway (Bionic / Ollama / Groq / Gemini)

`com.slotmind.ai.LLMGateway` exposes `callLLM(prompt)` and keeps provider-specific HTTP formats out of the scheduling
strategy. Select a provider with `CAREBRIDGE_AI_PROVIDER`:

- `bionic` — local OpenAI-compatible server, default `http://localhost:1234/v1`
- `ollama` — Ollama OpenAI-compatible API, default `http://localhost:11434/v1`
- `groq` — Groq API, default `https://api.groq.com/openai/v1`
- `gemini` — Gemini REST API, default `https://generativelanguage.googleapis.com/v1beta`

Example for Bionic + Qwen:

```bash
export SCHEDULING_STRATEGY=ai
export CAREBRIDGE_AI_PROVIDER=bionic
export CAREBRIDGE_AI_BASE_URL=http://localhost:1234/v1
export CAREBRIDGE_AI_MODEL='YOUR_EXACT_MODEL_ID_FROM_BIONIC'
# Only needed if Bionic API authentication is enabled:
export CAREBRIDGE_AI_API_KEY=''
mvn spring-boot:run
```

Check the model ID reported by Bionic's `/v1/models` endpoint rather than assuming the downloaded filename is the API
ID. For Ollama, use the exact installed model name. For Groq and Gemini, set `CAREBRIDGE_AI_API_KEY` to the
corresponding provider API key and configure `CAREBRIDGE_AI_MODEL` if you do not want the default model.

### Demo: take Dr. Sarah Chen offline

With the application running and the seed data loaded, this changes `PRV-001` to `OFFLINE`. The asynchronous agent loop
should process `APT-001` and `APT-002` and create pending reschedule suggestions when eligible replacement slots exist.

```bash
curl -X PATCH http://localhost:8080/providers/PRV-001/status \
  -H 'Content-Type: application/json' \
  -d '{"status":"OFFLINE"}'

# Check appointments and suggestions after the async worker has had time to run:
curl 'http://localhost:8080/appointments?status=RESCHEDULE_PENDING'
```

The status endpoint returns without waiting for the rescheduling loop. Suggestion generation can take longer when it
calls a local or remote LLM. Accepting a suggestion remains a separate front-desk action.
