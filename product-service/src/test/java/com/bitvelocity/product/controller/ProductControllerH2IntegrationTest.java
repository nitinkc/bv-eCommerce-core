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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Fast integration tests using H2 in-memory database.
 * Run these tests locally without Docker.
 *
 * To run: mvn test -Dtest=ProductControllerH2IntegrationTest
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ProductController Integration Tests (H2)")
class ProductControllerH2IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private CreateProductRequest createRequest;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        productRepository.deleteAll();

        createRequest = new CreateProductRequest();
        createRequest.setSku("TEST-SKU-001");
        createRequest.setName("Test Product");
        createRequest.setDescription("Test Description");
        createRequest.setPrice(new BigDecimal("99.99"));
        createRequest.setCategory("Electronics");
        createRequest.setStockQuantity(10);
        createRequest.setImageUrl("https://example.com/image.jpg");
        createRequest.setStatus(ProductStatus.ACTIVE);
    }

    @Test
    @Order(1)
    @DisplayName("Should create product successfully")
    void testCreateProduct() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("TEST-SKU-001"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.stockQuantity").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @Order(2)
    @DisplayName("Should get all products with pagination")
    void testGetAllProducts() throws Exception {
        // Create a product first
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].sku").value("TEST-SKU-001"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @Order(3)
    @DisplayName("Should search products by query")
    void testSearchProducts() throws Exception {
        // Create a product first
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        mockMvc.perform(get("/products/search")
                        .param("query", "Test"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    @Order(4)
    @DisplayName("Should get products by category")
    void testGetProductsByCategory() throws Exception {
        // Create a product first
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        mockMvc.perform(get("/products/category/Electronics")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("Electronics"));
    }

    @Test
    @Order(5)
    @DisplayName("Should update product stock")
    void testUpdateProductStock() throws Exception {
        // Create a product first
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(response).get("id").asText();

        UpdateStockRequest stockRequest = new UpdateStockRequest();
        stockRequest.setStockQuantity(25);

        mockMvc.perform(patch("/products/" + productId + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(25));
    }

    @Test
    @Order(6)
    @DisplayName("Should return 404 when product not found")
    void testGetProductNotFound() throws Exception {
        mockMvc.perform(get("/products/550e8400-e29b-41d4-a716-446655440000"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @DisplayName("Should return 409 when creating product with duplicate SKU")
    void testCreateProductDuplicateSku() throws Exception {
        // Create first product
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        // Try to create second product with same SKU
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @Order(8)
    @DisplayName("Should validate product creation with invalid data")
    void testCreateProductInvalidData() throws Exception {
        CreateProductRequest invalidRequest = new CreateProductRequest();
        invalidRequest.setSku(""); // Invalid: empty SKU
        invalidRequest.setName("AB"); // Invalid: too short
        invalidRequest.setPrice(new BigDecimal("-10.00")); // Invalid: negative price
        invalidRequest.setCategory("");
        invalidRequest.setStockQuantity(-5); // Invalid: negative stock

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @Order(9)
    @DisplayName("Should update product successfully")
    void testUpdateProduct() throws Exception {
        // Create a product first
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(response).get("id").asText();

        UpdateProductRequest updateRequest = new UpdateProductRequest();
        updateRequest.setName("Updated Product Name");
        updateRequest.setPrice(new BigDecimal("149.99"));

        mockMvc.perform(put("/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product Name"))
                .andExpect(jsonPath("$.price").value(149.99));
    }

    @Test
    @Order(10)
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() throws Exception {
        // Create a product first
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(response).get("id").asText();

        // Delete the product
        mockMvc.perform(delete("/products/" + productId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify it's deleted
        mockMvc.perform(get("/products/" + productId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @DisplayName("Should get active products only")
    void testGetActiveProducts() throws Exception {
        // Create an active product
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        // Create a discontinued product
        CreateProductRequest discontinuedRequest = new CreateProductRequest();
        discontinuedRequest.setSku("TEST-SKU-002");
        discontinuedRequest.setName("Discontinued Product");
        discontinuedRequest.setDescription("Test Description");
        discontinuedRequest.setPrice(new BigDecimal("79.99"));
        discontinuedRequest.setCategory("Electronics");
        discontinuedRequest.setStockQuantity(5);
        discontinuedRequest.setStatus(ProductStatus.DISCONTINUED);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(discontinuedRequest)));

        // Get only active products
        mockMvc.perform(get("/products/active")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @Order(12)
    @DisplayName("Should auto-update status to OUT_OF_STOCK when stock reaches 0")
    void testAutoStatusUpdateOutOfStock() throws Exception {
        // Create a product first
        String response = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(response).get("id").asText();

        // Update stock to 0
        UpdateStockRequest stockRequest = new UpdateStockRequest();
        stockRequest.setStockQuantity(0);

        mockMvc.perform(patch("/products/" + productId + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(0))
                .andExpect(jsonPath("$.status").value("OUT_OF_STOCK"));
    }

    @Test
    @Order(13)
    @DisplayName("Should sort products by price ascending")
    void testSortProductsByPrice() throws Exception {
        // Create multiple products
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)));

        CreateProductRequest expensiveProduct = new CreateProductRequest();
        expensiveProduct.setSku("TEST-SKU-002");
        expensiveProduct.setName("Expensive Product");
        expensiveProduct.setDescription("Test Description");
        expensiveProduct.setPrice(new BigDecimal("199.99"));
        expensiveProduct.setCategory("Electronics");
        expensiveProduct.setStockQuantity(5);
        expensiveProduct.setStatus(ProductStatus.ACTIVE);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expensiveProduct)));

        // Get products sorted by price ascending
        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "price")
                        .param("sortDir", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].price").value(99.99))
                .andExpect(jsonPath("$.content[1].price").value(199.99));
    }
}

