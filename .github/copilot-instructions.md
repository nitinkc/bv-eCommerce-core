# Copilot Instructions for Agentic AI – BitVelocity eCommerce Core

## 1. Bounded Contexts & Services
- Implement each service as a distinct module/microservice (see table in architecture doc).
- Respect clear separation of concerns: e.g., product-service for product CRUD, cart-service for cart management, etc.
- Use the initial status/phase to prioritize implementation order.

## 2. Protocols & Interfaces
- Use REST for CRUD operations (products, orders, carts).
- Use GraphQL for aggregated product/inventory queries (later phase).
- Use gRPC for low-latency internal calls (inventory reservation/release).
- Use Kafka for event-driven communication (order, inventory, product events).
- Use SSE and WebSocket for real-time updates (flash sales, order status).
- Use SOAP (simulated) for payment integration (phase 4+).
- Use webhooks for outbound partner notifications.
- Plan on using NATS for async communication in later phases with other domains

## 3. Data & Event Flow
- All writes go to Postgres (OLTP). No dual writes.
- Derived projections (Cassandra, Redis, OpenSearch, ClickHouse) are built from events/CDC only.
- Use canonical event envelopes as shown in the architecture doc.
- Use Debezium for CDC where required.

## 4. Caching
- Use read-through/event-driven caches for products, inventory, order status, flash sale prices.
- Invalidate caches only via event consumption (no side-channels).
- Use short TTL for GraphQL aggregated cache.

## 5. Resilience & Retry
- Implement exponential backoff, circuit breaker, and DLQ patterns as specified per operation.
- Use progressive retry for webhooks and event publishing.

## 6. Security
- Require JWT authentication for all mutations.
- Enforce authorization: e.g., only order owner can cancel PENDING orders.
- Sensitive actions (e.g., price override) require ADMIN role.
- Implement idempotency for order creation (hash key + payload, 24h window).

## 7. Observability
- Emit metrics, traces, and logs as specified (see section 10 in architecture doc).
- Correlate logs with orderId, traceId, userId, eventType.

## 8. Testing
- Cover all layers: unit, integration, contract, BDD, performance, security, fuzz, chaos (see testing matrix).
- Use JUnit, Testcontainers, Pact, Cucumber, Gatling, Spring Test, Jazzer as appropriate.

## 9. Implementation Phases
- Follow the phase plan for deliverables and priorities.
- Defer Cassandra, OpenSearch, and advanced features until after stable eventing (phase 5+).

## 10. General Guidance
- All events must be validated against schema registry.
- Do not leak internal table names in GraphQL.
- Pin and publish gRPC proto versions.
- Document REST endpoints with status codes and error models.
- Document and test idempotency semantics.
- Test replay procedures and security denial scenarios.

