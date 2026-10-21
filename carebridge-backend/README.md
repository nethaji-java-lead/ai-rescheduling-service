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

## AI mode
Set environment variables before startup:
```bash
export SCHEDULING_STRATEGY=ai
export OPENAI_API_KEY=your-key
mvn spring-boot:run
```
Use only a key for the configured provider/model. Do not commit credentials.

## Scope notes
The event processing uses in-process Spring events and asynchronous listeners. This is suitable for a local assessment demonstration, but events can be lost if the process stops. Production hardening should use a transactional outbox and durable message broker. H2 is in-memory and resets when the app restarts.
