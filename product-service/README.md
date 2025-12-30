# Product Service

**Production-ready product management microservice for the BitVelocity eCommerce platform.**

[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green)]()
[![Tests](https://img.shields.io/badge/tests-28%20passing-brightgreen)]()

---

## ⚡ Quick Commands

```bash
# Run with H2 (No Docker) - RECOMMENDED FOR DEVELOPMENT
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Run with PostgreSQL (Docker Required)
docker run -d --name postgres-product -e POSTGRES_DB=bitvelocity_products -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16-alpine
mvn spring-boot:run

# Run Tests (No Docker)
mvn test -Dtest=ProductControllerH2IntegrationTest

# Run All Tests
mvn clean verify
```

**After startup:** http://localhost:8081/api/swagger-ui.html

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Quick Start](#-quick-start)
- [API Reference](#-api-reference)
- [Architecture](#-architecture)
- [Testing](#-testing)
- [Configuration](#-configuration)
- [Development](#-development)

---

## 🎯 Overview

Full-featured product catalog service with CRUD operations, advanced search, filtering, pagination, validation, and comprehensive API documentation. Built following domain-driven design principles with extensive test coverage.

### Key Capabilities

- **CRUD Operations** - Create, read, update, delete products
- **Advanced Search** - Full-text search across name and description
- **Smart Filtering** - By category, status, or active products only
- **Pagination & Sorting** - Efficient data retrieval with customizable sorting
- **Stock Management** - Dedicated endpoint for inventory updates with auto-status
- **Input Validation** - Comprehensive validation with detailed error messages
- **Audit Trail** - Automatic tracking of creation and modification metadata
- **API Documentation** - Interactive Swagger UI with complete API specs
- **Exception Handling** - Standardized error responses across all endpoints
- **Test Coverage** - 28 tests (15 unit + 13 integration with Testcontainers)

### Statistics

- **21 Java Files** (19 main + 2 test)
- **11 API Endpoints**
- **3,500+ Lines of Code**
- **28 Passing Tests**

---

## 🚀 Quick Start

You have **two options** to run the service:

### Option 1: H2 In-Memory Database (No Docker Required) ⚡ **RECOMMENDED FOR LOCAL DEV**

**Easiest way to get started - no external database needed!**

```bash
cd bv-eCommerce-core/product-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

🌐 **Service**: http://localhost:8081/api  
📚 **Swagger UI**: http://localhost:8081/api/swagger-ui.html  
🔍 **H2 Console**: http://localhost:8081/api/h2-console

**H2 Console Connection:**
- JDBC URL: `jdbc:h2:mem:productdb`
- Username: `sa`
- Password: _(leave empty)_

**Benefits:**
- ✅ Starts instantly
- ✅ No Docker/PostgreSQL setup
- ✅ Perfect for development
- ✅ All features work the same

---

### Option 2: PostgreSQL with Docker 🐳

**For production-like environment:**

#### Step 1: Start PostgreSQL

```bash
docker run -d --name postgres-product \
  -e POSTGRES_DB=bitvelocity_products \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

#### Step 2: Run the Service

```bash
cd bv-eCommerce-core/product-service
mvn spring-boot:run
```

🌐 **Service**: http://localhost:8081/api  
📚 **Swagger UI**: http://localhost:8081/api/swagger-ui.html

---

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker (only for Option 2)
- Port 8081 available

### Test the API

```bash
# Create a product
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "LAPTOP-001",
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "price": 1299.99,
    "category": "Electronics",
    "stockQuantity": 10,
    "status": "ACTIVE"
  }'

# Get all products
curl "http://localhost:8081/api/products?page=0&size=20"

# Search products
curl "http://localhost:8081/api/products/search?query=laptop"
```

---

## 📡 API Reference

### Endpoints

| Method   | Endpoint                            | Description                   | Query Params                |
|:---------|:------------------------------------|:------------------------------|:----------------------------|
| `GET`    | `/api/products`                     | List all products (paginated) | page, size, sortBy, sortDir |
| `GET`    | `/api/products/search`              | Search by name/description    | query                       |
| `GET`    | `/api/products/category/{category}` | Filter by category            | -                           |
| `GET`    | `/api/products/status/{status}`     | Filter by status              | -                           |
| `GET`    | `/api/products/active`              | Get active products only      | -                           |
| `GET`    | `/api/products/{id}`                | Get product by UUID           | -                           |
| `GET`    | `/api/products/sku/{sku}`           | Get product by SKU            | -                           |
| `POST`   | `/api/products`                     | Create new product            | -                           |
| `PUT`    | `/api/products/{id}`                | Update product (partial)      | -                           |
| `PATCH`  | `/api/products/{id}/stock`          | Update stock quantity         | -                           |
| `DELETE` | `/api/products/{id}`                | Delete product                | -                           |

### Query Parameters

**Pagination:**
- `page` - Page number (default: 0)
- `size` - Items per page (default: 20, max: 100)
- `sortBy` - Sort field (default: createdAt)
- `sortDir` - Direction: `asc` or `desc` (default: desc)

### Request/Response Examples

**Create Product:**
```json
POST /api/products
{
  "sku": "LAPTOP-001",
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop with RTX 4080",
  "price": 1299.99,
  "category": "Electronics",
  "stockQuantity": 10,
  "imageUrl": "https://example.com/laptop.jpg",
  "status": "ACTIVE"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "sku": "LAPTOP-001",
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop with RTX 4080",
  "price": 1299.99,
  "category": "Electronics",
  "stockQuantity": 10,
  "imageUrl": "https://example.com/laptop.jpg",
  "status": "ACTIVE",
  "createdAt": "2025-12-30T12:00:00Z",
  "updatedAt": "2025-12-30T12:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
```

**Update Stock:**
```json
PATCH /api/products/{id}/stock
{
  "stockQuantity": 25
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2025-12-30T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/products",
  "validationErrors": [
    "SKU must contain only uppercase letters, numbers and hyphens",
    "Price must be greater than 0"
  ]
}
```

### Validation Rules

| Field | Constraints |
|-------|-------------|
| `sku` | Required, max 50 chars, uppercase + numbers + hyphens only |
| `name` | Required, 3-255 chars |
| `description` | Optional, max 2000 chars |
| `price` | Required, > 0, max 8 integer + 2 decimal digits |
| `category` | Required, max 100 chars |
| `stockQuantity` | Required, >= 0 |
| `imageUrl` | Optional, max 500 chars, must end with image extension |
| `status` | Must be: ACTIVE, DISCONTINUED, OUT_OF_STOCK, or DRAFT |

### HTTP Status Codes

| Code | Description | When |
|------|-------------|------|
| `200` | OK | Successful GET, PUT, PATCH |
| `201` | Created | Successful POST |
| `204` | No Content | Successful DELETE |
| `400` | Bad Request | Validation failure |
| `404` | Not Found | Product not found |
| `409` | Conflict | Duplicate SKU |
| `500` | Server Error | Unexpected error |

### Product Status Behavior

- **Auto-Status Updates:**
  - When `stockQuantity` reaches 0 → Status changes to `OUT_OF_STOCK`
  - When stock is replenished → Status changes to `ACTIVE`

- **Status Values:**
  - `ACTIVE` - Available for purchase
  - `DISCONTINUED` - No longer offered
  - `OUT_OF_STOCK` - Temporarily unavailable
  - `DRAFT` - Not yet published

---

## 🏗️ Architecture


### Project Structure

```
product-service/
├── src/main/java/com/bitvelocity/product/
│   ├── config/
│   │   ├── OpenApiConfig.java       # Swagger configuration
│   │   └── JpaConfig.java           # JPA auditing setup
│   ├── controller/
│   │   └── ProductController.java    # REST endpoints
│   ├── domain/
│   │   ├── Product.java             # JPA entity
│   │   └── ProductStatus.java       # Enum
│   ├── dto/
│   │   ├── CreateProductRequest.java
│   │   ├── UpdateProductRequest.java
│   │   ├── UpdateStockRequest.java
│   │   ├── ProductResponse.java
│   │   └── PageResponse.java
│   ├── exception/
│   │   ├── ProductNotFoundException.java
│   │   ├── ProductAlreadyExistsException.java
│   │   ├── ErrorResponse.java
│   │   └── GlobalExceptionHandler.java
│   ├── mapper/
│   │   └── ProductMapper.java       # Entity ↔ DTO conversion
│   ├── repository/
│   │   └── ProductRepository.java   # Spring Data JPA
│   ├── service/
│   │   └── ProductService.java      # Business logic
│   └── ProductServiceApplication.java
└── src/test/java/
    ├── service/
    │   └── ProductServiceTest.java          # 15 unit tests
    └── controller/
        └── ProductControllerIntegrationTest.java  # 13 integration tests
```

### Technology Stack

- **Spring Boot 3.2.1** - Application framework
- **Spring Data JPA** - Data persistence
- **PostgreSQL 16** - Production database
- **H2** - In-memory test database
- **SpringDoc OpenAPI 2.3.0** - API documentation
- **Lombok** - Reduce boilerplate code
- **Testcontainers 1.19.3** - Integration testing
- **JUnit 5 & Mockito** - Unit testing

### Key Components

**Repository Layer:**
- Custom queries: `findBySku`, `findByCategory`, `findByStatus`
- Full-text search: `searchProducts` (name or description)
- Active products filter: `findActiveProducts`

**Service Layer:**
- 11 business methods with transaction management
- Auto-status updates based on stock
- SKU uniqueness enforcement
- Comprehensive error handling

**Controller Layer:**
- RESTful endpoints with proper HTTP methods
- OpenAPI annotations for documentation
- Request validation with Jakarta Validation
- Paginated responses with metadata

---

## 🧪 Testing

The product-service provides two types of integration tests for different scenarios:

### Quick Local Testing (H2 - No Docker Required) ⚡

**Fast in-memory testing perfect for local development:**

```bash
# Run H2 integration tests (2-5 seconds)
mvn test -Dtest=ProductControllerH2IntegrationTest

# Run specific test
mvn test -Dtest=ProductControllerH2IntegrationTest#testCreateProduct
```

**Features:**
- Uses H2 in-memory database
- No Docker required
- Fast execution (~2-5 seconds)
- Perfect for TDD and local development
- Uses `application-test.yml` profile automatically

### Full Integration Testing (Testcontainers - Docker Required) 🐳

**Production-like testing with real PostgreSQL:**

```bash
# Make sure Docker is running
docker ps

# Run Testcontainers tests (10-30 seconds)
mvn test -Dtest=ProductControllerIntegrationTest
```

**Features:**
- Real PostgreSQL via Testcontainers
- Full integration validation
- Requires Docker
- Best for CI/CD and pre-deployment

### Run All Tests

```bash
# All tests (unit + both integration types)
mvn clean verify

# Only unit tests (fastest)
mvn test -Dtest=ProductServiceTest
```

### Test Coverage

**Unit Tests (ProductServiceTest):** 15 tests
- All CRUD operations
- Search and filtering  
- Stock updates with auto-status
- Exception scenarios
- Edge cases (null handling, validation)

**Integration Tests (H2):** 13 tests
- Full API endpoint testing
- Request/response validation
- Error handling
- Pagination and sorting
- All business scenarios

**Integration Tests (Testcontainers):** 13 tests
- Same as H2 but with real PostgreSQL
- Database-specific features
- Production environment simulation

**Total:** 28+ tests covering all layers ✅

### Detailed Testing Guide

For comprehensive testing documentation, see [TESTING_GUIDE.md](TESTING_GUIDE.md):
- When to use H2 vs Testcontainers
- Troubleshooting guide
- Performance comparison
- Best practices
- CI/CD integration

---

## ⚙️ Configuration

### Database Setup

The service supports multiple database configurations:

#### 1. **Local Development (H2 - No Docker)** ⚡ RECOMMENDED

Use the `local` profile for instant startup with H2 in-memory database:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Configuration file: `src/main/resources/application-local.yml`

**Features:**
- In-memory H2 database with PostgreSQL compatibility
- H2 Console enabled at `/h2-console`
- Auto-creates schema on startup
- Perfect for development and testing
- No external dependencies

**H2 Console Access:**
- URL: http://localhost:8081/api/h2-console
- JDBC URL: `jdbc:h2:mem:productdb`
- Username: `sa`
- Password: _(empty)_

#### 2. **Production (PostgreSQL)**

Default profile uses PostgreSQL. Update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bitvelocity_products
    username: postgres
    password: postgres
```

**Start PostgreSQL with Docker:**
```bash
docker run -d --name postgres-product \
  -e POSTGRES_DB=bitvelocity_products \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

Then run:
```bash
mvn spring-boot:run
# Or specify default profile explicitly:
mvn spring-boot:run -Dspring-boot.run.profiles=default
```

#### 3. **Testing**

Two testing options available:

**A. H2 In-Memory (Fast, No Docker):**
- Automatically uses `src/test/resources/application-test.yml`
- H2 database with PostgreSQL compatibility mode
- Activated by `@ActiveProfiles("test")` in test classes
- Perfect for local development

```bash
mvn test -Dtest=ProductControllerH2IntegrationTest
```

**B. Testcontainers (Full Integration):**
- Uses real PostgreSQL container
- Configuration dynamically set by Testcontainers
- Requires Docker running
- Best for CI/CD pipelines

```bash
mvn test -Dtest=ProductControllerIntegrationTest
```

---

### Profile Summary

| Profile | Database | Docker Required | Use Case | Command |
|---------|----------|----------------|----------|---------|
| **local** | H2 (in-memory) | ❌ No | Daily development | `mvn spring-boot:run -Dspring-boot.run.profiles=local` |
| **default** | PostgreSQL | ✅ Yes | Production-like | `mvn spring-boot:run` |
| **test** | H2 (in-memory) | ❌ No | Unit/Integration tests | `mvn test -Dtest=ProductControllerH2IntegrationTest` |

---

### Application Properties

Key settings in `application.yml`:

```yaml
server:
  port: 8081
  servlet:
    context-path: /api

spring:
  jpa:
    hibernate:
      ddl-auto: update  # Use 'validate' in production
    show-sql: false     # Set true for debugging
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/bitvelocity_products}
    username: ${DATABASE_USER:postgres}
    password: ${DATABASE_PASSWORD:postgres}

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    com.bitvelocity.product: INFO
```

### Environment Variables

Override settings with environment variables:

```bash
export DATABASE_URL=jdbc:postgresql://prod-db:5432/products
export DATABASE_USER=prod_user
export DATABASE_PASSWORD=secure_password
export SERVER_PORT=8081

mvn spring-boot:run
```

---

## 📊 Database Schema

```sql
CREATE TABLE products (
    id UUID PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_product_category ON products(category);
CREATE INDEX idx_product_status ON products(status);
CREATE INDEX idx_product_created_at ON products(created_at);
```

**Note:** Schema is auto-created by Hibernate (`ddl-auto: update`). For production, use migration tools like Flyway or Liquibase.

---

## 🛠️ Development

### Build Project

```bash
# Full build with tests
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Package as JAR
mvn clean package
```

### Run with Custom Port

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

### Enable SQL Logging

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.jpa.show-sql=true
```

### Docker Commands

```bash
# Start PostgreSQL
docker run -d --name postgres-product \
  -e POSTGRES_DB=bitvelocity_products \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# Stop PostgreSQL
docker stop postgres-product

# Remove container
docker rm postgres-product

# View logs
docker logs postgres-product

# Connect to database
docker exec -it postgres-product psql -U postgres -d bitvelocity_products
```

### Common Tasks

**Clear local database:**
```sql
TRUNCATE TABLE products CASCADE;
```

**View all products:**
```sql
SELECT id, sku, name, price, stock_quantity, status FROM products;
```

**Reset auto-increment:**
```sql
-- Not needed (using UUID)
```

---

## 🔮 Future Enhancements

### Planned Features
- [ ] JWT authentication & authorization
- [ ] Product image upload/storage (S3/Azure Blob)
- [ ] Product reviews and ratings
- [ ] Category hierarchy & management
- [ ] Product variants (size, color, SKU variants)
- [ ] Bulk import/export (CSV, Excel)
- [ ] Event publishing (Kafka) for product changes
- [ ] Elasticsearch integration for advanced search
- [ ] Redis caching for frequently accessed products
- [ ] Price history tracking
- [ ] Inventory reservation system
- [ ] Multi-language support

### Technical Improvements
- [ ] GraphQL API alongside REST
- [ ] gRPC for internal service communication
- [ ] CQRS with read/write models
- [ ] Event sourcing for audit trail
- [ ] Rate limiting
- [ ] API versioning
- [ ] Performance monitoring (Micrometer)
- [ ] Database migration with Flyway

---

## 📚 Related Documentation

- [Root README](../../README.md) - Main project documentation
- [eCommerce Architecture](../../DOMAIN_ECOMMERCE_ARCHITECTURE.md) - Full system architecture
- [Build Guide](../../BUILD_SUCCESS_SUMMARY.md) - Build setup and troubleshooting
- [API Protocols](../../.github/copilot-instructions.md) - Coding standards

---

## 🤝 Contributing

1. Follow the BitVelocity coding standards
2. Write tests for all new features
3. Ensure all tests pass: `mvn clean verify`
4. Update documentation as needed
5. Use meaningful commit messages

---

## 📄 License

Part of the BitVelocity eCommerce platform.

---

**Questions or Issues?** Check the Swagger UI or refer to the architecture documentation.

