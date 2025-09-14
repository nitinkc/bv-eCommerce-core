# Repository Scaffold – bv-ecommerce-core

> Purpose: E-Commerce backbone (Products, Carts, Orders, Inventory, Payment Adapter, Notifications, Webhooks, Pricing). OLTP Postgres → Events/CDC → Derived Stores (Redis/Cassandra/OpenSearch) → OLAP.

## 1. Directory Tree (Initial Skeleton)

```
bv-ecommerce-core/
  README.md
  ARCHITECTURE.md
  CONTRIBUTING.md
  LICENSE
  pom.xml
  docs/
    adr/
      ADR-001-multi-repo-vs-monorepo.md
      ADR-002-event-vs-cdc-strategy.md
      ADR-003-protocol-introduction-order.md
      ADR-004-oltp-cdc-olap-architecture.md
      ADR-005-security-layering.md
      ADR-006-retry-backoff-policies.md
      ADR-007-observability-baseline.md
      ADR-008-pulumi-cloud-provider-abstraction.md
    diagrams/
    api/
      rest/
      graphql/
      grpc/
    events/
      ecommerce.order.order.created.v1.json
      ecommerce.order.order.paid.v1.json
      ecommerce.inventory.stock.adjusted.v1.json
      ecommerce.product.product.updated.v1.json
  infra/
    pulumi/
      Pulumi.dev.yaml
      Pulumi.staging.yaml
      Pulumi.prod.yaml
      src/
        CloudProviderFactory.java
        LocalStack.java
        GcpStack.java
        AwsStack.java
        AzureStack.java
        PostgresModule.java
        KafkaModule.java
        RedisModule.java
        VaultModule.java
  libs/
    bv-ecom-domain-model/
    bv-ecom-application-core/
    bv-ecom-testing-support/
  services/
    product-service/
      src/main/java/...
      src/main/resources/db/migration/ (Flyway)
      src/test/java/...
      bdd/features/product_crud.feature
    cart-service/
    order-service/
      bdd/features/order_checkout.feature
    inventory-service/
      proto/inventory.proto
    payment-adapter-service/
      src/main/resources/wsdl/payment.wsdl (mock)
    notification-service/
      src/main/java/... (WebSocket & SSE)
    partner-webhook-dispatcher/
    pricing-service/ (optional early merge into product)
  test-support/
    docker-compose.dev.yml
    testcontainers/
  bdd/
    features/
      checkout_success.feature
      cancel_order_releases_stock.feature
  contracts/
    rest/
    grpc/
  scripts/
    local-dev/
      start-core.sh
      stop-core.sh
      seed-data.sh
    replay/
      run-replay.sh
    cost/
      prune-images.sh
  config/
    application-dev.yaml
    application-test.yaml
    application-local.yaml
  .github/
    workflows/
      ci.yml
      security-scan.yml
      contract-tests.yml
      nightly-replay-check.yml
  security/
    opa/
      policies/
        order-cancel.rego
    vault/
      policies/
        ecom-orders.hcl
  performance/
    gatling/
      OrderCheckoutSimulation.scala
  fuzz/
    jazzer/
      OrderPayloadFuzzTest.java
```

## 2. Service Responsibilities Summary
| Service | Key Protocols | DB Tables (Primary) | Events Produced |
|---------|---------------|---------------------|-----------------|
| product-service | REST / GraphQL | products, product_price_history | product.updated |
| cart-service | REST | carts, cart_items | (minimal events) |
| order-service | REST / Kafka / gRPC (inventory) | orders, order_items, order_status_history | order.created/paid/canceled/fulfilled |
| inventory-service | gRPC / MQTT | inventory_stock, inventory_adjustment | inventory.stock.adjusted |
| payment-adapter-service | SOAP / REST | payments | order.paid (via order-service) |
| notification-service | WebSocket / SSE | (Redis ephemeral) | none (consumes) |
| partner-webhook-dispatcher | Webhooks / AMQP | webhook_subscriptions, webhook_delivery_attempts | webhook.delivery.failed |
| pricing-service | REST / SSE / Kafka | flash_sale_state | pricing.flash_sale.started |

## 3. Testing Layers
| Layer | Folder | Tooling |
|-------|--------|---------|
| Unit | service/src/test | JUnit, AssertJ, Mockito |
| Integration | service/src/test (Testcontainers profile) | Testcontainers (Postgres, Kafka, Redis) |
| Contract | contracts/rest & grpc | Spring Cloud Contract / Pact / protobuf golden |
| BDD | bdd/features | Cucumber |
| Performance | performance/gatling | Gatling |
| Security | .github/workflows/security-scan.yml | OWASP Dependency-Check, ZAP baseline |
| Fuzz | fuzz/jazzer | Jazzer |
| Replay Validation | scripts/replay | Custom harness |

## 4. Make Targets (Optional)
| Command | Description |
|---------|-------------|
| `./mvnw clean verify -Punit` | Unit tests only |
| `./mvnw verify -Pintegration` | Unit + integration (Testcontainers) |
| `./scripts/local-dev/start-core.sh` | Start Postgres + Kafka + Redis locally |
| `./scripts/replay/run-replay.sh orders_by_customer --from ...` | Sample projection replay |

## 5. Observability Conventions
- Metrics prefix: `ecom_`
- Traces: `OrderController#createOrder`, `InventoryGrpc#ReserveStock`
- Log fields: `orderId`, `correlationId`, `traceId`, `eventType`

## 6. Initial Environment Variables
| Var | Purpose |
|-----|---------|
| POSTGRES_URL | JDBC connection |
| KAFKA_BROKERS | Kafka bootstrap servers |
| REDIS_HOST | Redis cache |
| VAULT_ADDR | Vault integration (phase) |
| OPA_URL | Policy decisions |
| ORDER_WS_ALLOWED_ORIGINS | WebSocket security |

## 7. Phase Flags
| Flag | Description |
|------|-------------|
| FEATURE_PRICING | Enable pricing-service |
| FEATURE_PAYMENT_SOAP | Enable SOAP adapter |
| FEATURE_IOT_MQTT | Enable MQTT ingestion |
| FEATURE_WEBHOOKS | Enable webhook dispatcher |

## 8. Notes
Keep business logic deliberately thin—focus on reliability, protocol correctness, and data propagation.
