# Copilot Instructions for Agentic AI – BitVelocity eCommerce Core

## 0. Project Setup & Build
- **Parent POM**: Uses `spring-boot-starter-parent` (3.2.1) directly for simplicity
- **Java Version**: 21 (required)
- **Maven**: Multi-module project structure
- **Build Command**: `mvn clean install` (all modules build together)
- **H2 for Runtime**: H2 dependency scope is `runtime` (not just test) to support local development

## 1. Bounded Contexts & Services
- Implement each service as a distinct Maven module/microservice (see table in architecture doc)
- Respect clear separation of concerns: e.g., product-service for product CRUD, cart-service for cart management, etc.
- Use the initial status/phase to prioritize implementation order
- **Current Services**: product-service, cart-service, order-service, inventory-service, notification-service, pricing-service, payment-adapter-service, partner-webhook-dispatcher, analytics-streaming-service, replay-service

## 2. Protocols & Interfaces
- Use REST for CRUD operations (products, orders, carts)
- Use GraphQL for aggregated product/inventory queries (later phase)
- Use gRPC for low-latency internal calls (inventory reservation/release)
- Use Kafka for event-driven communication (order, inventory, product events)
- Use SSE and WebSocket for real-time updates (flash sales, order status)
- Use SOAP (simulated) for payment integration (phase 4+)
- Use webhooks for outbound partner notifications
- Plan on using NATS for async communication in later phases with other domains
- **OpenAPI/Swagger**: Use SpringDoc (2.3.0) for REST API documentation

## 3. Data & Event Flow
- All writes go to Postgres (OLTP). No dual writes
- Derived projections (Cassandra, Redis, OpenSearch, ClickHouse) are built from events/CDC only
- Use canonical event envelopes as shown in the architecture doc
- Use Debezium for CDC where required

## 4. Local Development & Profiles
- **Local Profile**: Use H2 in-memory database for development without Docker
  - Command: `mvn spring-boot:run -Dspring-boot.run.profiles=local`
  - Configuration: `application-local.yml` with H2 setup
  - H2 Console: Available at `/h2-console` when running locally
- **Default Profile**: Uses PostgreSQL (requires Docker)
  - Command: `mvn spring-boot:run`
  - Configuration: `application.yml` with PostgreSQL setup
- **Test Profile**: Uses H2 for fast testing
  - Configuration: `application-test.yml`
  - Activated automatically by `@ActiveProfiles("test")` in test classes

## 5. Database Strategy
- **Local Development**: H2 with PostgreSQL compatibility mode (no Docker required)
- **Testing**: Two options
  - H2 integration tests (fast, no Docker): `mvn test -Dtest=*H2IntegrationTest`
  - Testcontainers tests (PostgreSQL, requires Docker): `mvn test -Dtest=*IntegrationTest`
- **Production**: PostgreSQL (OLTP primary database)
- **Schema Management**: JPA `ddl-auto: update` for development, use Flyway/Liquibase for production

## 6. Caching
- Use read-through/event-driven caches for products, inventory, order status, flash sale prices
- Invalidate caches only via event consumption (no side-channels)
- Use short TTL for GraphQL aggregated cache

## 7. Resilience & Retry
- Implement exponential backoff, circuit breaker, and DLQ patterns as specified per operation
- Use progressive retry for webhooks and event publishing

## 8. Security
- Require JWT authentication for all mutations
- Enforce authorization: e.g., only order owner can cancel PENDING orders
- Sensitive actions (e.g., price override) require ADMIN role
- Implement idempotency for order creation (hash key + payload, 24h window)

## 9. Observability
- Emit metrics, traces, and logs as specified (see section 10 in architecture doc)
- Correlate logs with orderId, traceId, userId, eventType
- Enable SQL logging in development: `spring.jpa.show-sql: true`

## 10. Testing Strategy
- **Unit Tests**: Mock dependencies, test business logic (JUnit 5, Mockito)
- **H2 Integration Tests**: Fast, no Docker, use `@ActiveProfiles("test")`
  - Test class naming: `*H2IntegrationTest.java`
  - Run with: `mvn test -Dtest=*H2IntegrationTest`
- **Testcontainers Tests**: Full PostgreSQL, requires Docker
  - Test class naming: `*IntegrationTest.java`
  - Run with: `mvn test -Dtest=*IntegrationTest`
- Cover all layers: unit, integration, contract, BDD, performance, security, fuzz, chaos (see testing matrix)
- Use JUnit, Testcontainers, Pact, Cucumber, Gatling, Spring Test, Jazzer as appropriate
- **Recommended workflow**: Use H2 tests for TDD/local development, Testcontainers for CI/CD validation

## 11. Documentation Standards
- **Keep documentation minimal and consolidated**
- Each service should have ONE comprehensive README.md with:
  - Quick commands section at the top (copy-paste ready)
  - Quick Start with H2 (local) and PostgreSQL (Docker) options
  - API reference with examples
  - Testing guide
  - Configuration (profiles)
  - Development commands
- **Avoid creating multiple documentation files** (no separate "quick start", "setup guide", "troubleshooting", etc.)
- Add essential information to README, remove redundant docs
- Quick commands should show both H2 (no Docker) and PostgreSQL (Docker) options

## 12. Implementation Phases
- Follow the phase plan for deliverables and priorities
- Defer Cassandra, OpenSearch, and advanced features until after stable eventing (phase 5+)

## 13. General Guidance
- All events must be validated against schema registry
- Do not leak internal table names in GraphQL
- Pin and publish gRPC proto versions
- Document REST endpoints with status codes and error models (use OpenAPI annotations)
- Document and test idempotency semantics
- Test replay procedures and security denial scenarios
- **Build Verification**: Always run `mvn clean install` to verify all modules build
- **Local Development**: Prioritize H2 profile for fast iteration
- **Production Testing**: Use PostgreSQL/Testcontainers before deployment

