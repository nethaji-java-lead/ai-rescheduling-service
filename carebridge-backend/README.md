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
- Front-desk rescheduling desk: http://localhost:8080/
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:carebridgedb`
- User: `sa`; password blank

## Example requests

Create appointment:
```bash
curl -X POST http://localhost:8080/appointments -H "Content-Type: application/json" -d '{"patientName":"Taylor Smith","type":"ROUTINE","providerId":1,"slotId":2}'
```

Provider offline (automatically triggers asynchronous rescheduling):
```bash
curl -X PATCH http://localhost:8080/providers/1/status -H "Content-Type: application/json" -d '{"status":"OFFLINE"}'
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
curl -X PATCH http://localhost:8080/slots/1/status -H "Content-Type: application/json" -d '{"status":"CANCELLED"}'
```

Mark a room unavailable (automatically triggers suggestions for booked appointments in that room):
```bash
curl -X PATCH http://localhost:8080/rooms/1/availability -H "Content-Type: application/json" -d '{"available":false}'
```

The front-desk desk refreshes pending suggestions automatically and supports accepting or rejecting them. Accepting a suggestion locks the appointment and replacement slot, rechecks provider/room availability, and commits the appointment and slot updates in one transaction.

## Local AI mode (Bionic + Qwen3-VL)
The AI strategy uses Spring AI's OpenAI-compatible chat client pointed at Bionic's local inference endpoint. It does not require an OpenAI account or an OpenAI API key. The default configuration expects the local server at `http://localhost:1234/v1` and uses model identifier `wareef/Qwen3-VL-8B-Instruct-Q4_K_M-GGUF`.

Start Bionic with the Qwen3-VL model loaded and its OpenAI-compatible chat/completions API enabled, then run the backend with:
```powershell
$env:SCHEDULING_STRATEGY = "ai"
# Set these if Bionic uses a different address or reports a different model identifier:
$env:LOCAL_LLM_BASE_URL = "http://localhost:1234/v1"
$env:LOCAL_LLM_MODEL = "wareef/Qwen3-VL-8B-Instruct-Q4_K_M-GGUF"
mvn spring-boot:run
```
For a local server that requires authentication, set `LOCAL_LLM_API_KEY`. Otherwise the default `local` value is only a compatibility placeholder and is not an OpenAI credential. Keep the rule-based strategy as the default by omitting `SCHEDULING_STRATEGY` when you do not want to call the local model. AI errors fall back to rule-based recommendations.

## Scope notes
The event processing uses in-process Spring events and asynchronous listeners. This is suitable for a local assessment demonstration, but events can be lost if the process stops. Production hardening should use a transactional outbox and durable message broker. H2 is in-memory and resets when the app restarts.
