package com.bitvelocity.product.controller;

import com.bitvelocity.product.domain.ProductStatus;
import com.bitvelocity.product.dto.CreateProductRequest;
import com.bitvelocity.product.dto.UpdateProductRequest;
import com.bitvelocity.product.dto.UpdateStockRequest;
import com.bitvelocity.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration tests using Testcontainers with PostgreSQL.
 * Requires Docker to be running.
 *
 * For faster local testing without Docker, use ProductControllerH2IntegrationTest instead.
 *
 * To run: mvn test -Dtest=ProductControllerIntegrationTest
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ProductController Integration Tests (PostgreSQL/Testcontainers)")
class ProductControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private CreateProductRequest createRequest;
    private String createdProductId;

    @BeforeEach
    void setUp() {
        createRequest = CreateProductRequest.builder()
                .sku("LAPTOP-001")
                .name("Gaming Laptop")
                .description("High-performance gaming laptop with RTX 4080")
                .price(new BigDecimal("1299.99"))
                .category("Electronics")
                .stockQuantity(10)
                .imageUrl("http://example.com/laptop.jpg")
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.sku").value("LAPTOP-001"))
                .andExpect(jsonPath("$.name").value("Gaming Laptop"))
                .andExpect(jsonPath("$.price").value(1299.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stockQuantity").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.createdBy").value("system"));
    }

    @Test
    @Order(2)
    @DisplayName("Should fail to create product with duplicate SKU")
    void shouldFailToCreateProductWithDuplicateSku() throws Exception {
        // Create first product
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // Try to create duplicate
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    @Order(3)
    @DisplayName("Should fail validation when creating product with invalid data")
    void shouldFailValidationWhenCreatingWithInvalidData() throws Exception {
        CreateProductRequest invalidRequest = CreateProductRequest.builder()
                .sku("") // Blank SKU
                .name("AB") // Too short name
                .price(new BigDecimal("-10.00")) // Negative price
                .category("")
                .stockQuantity(-5) // Negative stock
                .build();

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors", hasSize(greaterThan(0))));
    }

    @Test
    @Order(4)
    @DisplayName("Should get all products with pagination")
    void shouldGetAllProductsWithPagination() throws Exception {
        // Create multiple products
        for (int i = 1; i <= 3; i++) {
            CreateProductRequest request = CreateProductRequest.builder()
                    .sku("PROD-00" + i)
                    .name("Product " + i)
                    .description("Description " + i)
                    .price(new BigDecimal("99.99"))
                    .category("Test")
                    .stockQuantity(5)
                    .status(ProductStatus.ACTIVE)
                    .build();

            mockMvc.perform(post("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Get all products
        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @Order(5)
    @DisplayName("Should get product by ID")
    void shouldGetProductById() throws Exception {
        // Create product
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String productId = objectMapper.readTree(response).get("id").asText();

        // Get by ID
        mockMvc.perform(get("/products/{id}", productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.sku").value("LAPTOP-001"));
    }

    @Test
    @Order(6)
    @DisplayName("Should return 404 when product not found by ID")
    void shouldReturn404WhenProductNotFoundById() throws Exception {
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/products/{id}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    @Order(7)
    @DisplayName("Should get product by SKU")
    void shouldGetProductBySku() throws Exception {
        // Create product
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // Get by SKU
        mockMvc.perform(get("/products/sku/{sku}", "LAPTOP-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("LAPTOP-001"))
                .andExpect(jsonPath("$.name").value("Gaming Laptop"));
    }

    @Test
    @Order(8)
    @DisplayName("Should search products")
    void shouldSearchProducts() throws Exception {
        // Create products
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // Search
        mockMvc.perform(get("/products/search")
                        .param("query", "gaming"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value(containsString("Gaming")));
    }

    @Test
    @Order(9)
    @DisplayName("Should get products by category")
    void shouldGetProductsByCategory() throws Exception {
        // Create product
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // Get by category
        mockMvc.perform(get("/products/category/{category}", "Electronics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @Order(10)
    @DisplayName("Should update product")
    void shouldUpdateProduct() throws Exception {
        // Create product
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String productId = objectMapper.readTree(response).get("id").asText();

        // Update product
        UpdateProductRequest updateRequest = UpdateProductRequest.builder()
                .name("Updated Gaming Laptop")
                .price(new BigDecimal("1199.99"))
                .build();

        mockMvc.perform(put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Gaming Laptop"))
                .andExpect(jsonPath("$.price").value(1199.99));
    }

    @Test
    @Order(11)
    @DisplayName("Should update product stock")
    void shouldUpdateProductStock() throws Exception {
        // Create product
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String productId = objectMapper.readTree(response).get("id").asText();

        // Update stock
        UpdateStockRequest stockRequest = new UpdateStockRequest(25);

        mockMvc.perform(patch("/products/{id}/stock", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(25));
    }

    @Test
    @Order(12)
    @DisplayName("Should delete product")
    void shouldDeleteProduct() throws Exception {
        // Create product
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String productId = objectMapper.readTree(response).get("id").asText();

        // Delete product
        mockMvc.perform(delete("/products/{id}", productId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(13)
    @DisplayName("Should get active products only")
    void shouldGetActiveProductsOnly() throws Exception {
        // Create active product
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        // Get active products
        mockMvc.perform(get("/products/active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }
}
