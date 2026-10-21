# ADR-001: CareBridge reactive rescheduling

## Context
CareBridge needs to react to provider unavailability and cancelled slots, propose safe alternatives, and leave final approval to front-desk staff.

## Decision
- Keep domain operations in services and expose DTOs from REST controllers.
- Share a `SchedulingEngine` contract between API-driven and event-driven scheduling.
- Use a deterministic rule-based strategy by default and an optional Spring AI strategy selected by configuration.
- Validate LLM recommendations against current slots, providers, room availability, and status before persisting.
- On AI failure, use rule-based recommendations.
- Publish events in the originating transaction and consume them after commit asynchronously.
- Require human approval. Accepting a suggestion updates appointment and slot state transactionally with pessimistic locking and JPA optimistic versions.
- Model rooms minimally so slot eligibility can account for room availability.

## Consequences
The implementation is straightforward to run locally with Java 17 and H2. In-process events are not durable across process restarts; a production implementation should persist an outbox record in the same transaction and dispatch it through a durable queue. H2 is not a substitute for production concurrency testing on the target database.

## Future extensions
A room constraint policy can be added to the scheduling eligibility contract. Waitlists, patient notifications, and durable event delivery are intentionally outside this sprint.
