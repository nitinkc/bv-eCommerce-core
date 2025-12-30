package com.bitvelocity.product.service;

import com.bitvelocity.product.domain.Product;
import com.bitvelocity.product.domain.ProductStatus;
import com.bitvelocity.product.dto.*;
import com.bitvelocity.product.exception.ProductAlreadyExistsException;
import com.bitvelocity.product.exception.ProductNotFoundException;
import com.bitvelocity.product.mapper.ProductMapper;
import com.bitvelocity.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private ProductResponse sampleResponse;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        
        sampleProduct = Product.builder()
                .id(productId)
                .sku("LAPTOP-001")
                .name("Gaming Laptop")
                .description("High-performance gaming laptop")
                .price(new BigDecimal("1299.99"))
                .category("Electronics")
                .stockQuantity(10)
                .imageUrl("http://example.com/laptop.jpg")
                .status(ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleResponse = ProductResponse.builder()
                .id(productId)
                .sku("LAPTOP-001")
                .name("Gaming Laptop")
                .description("High-performance gaming laptop")
                .price(new BigDecimal("1299.99"))
                .category("Electronics")
                .stockQuantity(10)
                .imageUrl("http://example.com/laptop.jpg")
                .status(ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateProductRequest.builder()
                .sku("LAPTOP-001")
                .name("Gaming Laptop")
                .description("High-performance gaming laptop")
                .price(new BigDecimal("1299.99"))
                .category("Electronics")
                .stockQuantity(10)
                .imageUrl("http://example.com/laptop.jpg")
                .status(ProductStatus.ACTIVE)
                .build();

        updateRequest = UpdateProductRequest.builder()
                .name("Updated Gaming Laptop")
                .price(new BigDecimal("1199.99"))
                .build();
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void shouldGetAllProductsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        
        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        // When
        PageResponse<ProductResponse> result = productService.getAllProducts(0, 20, "createdAt", "desc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getSku()).isEqualTo("LAPTOP-001");
        verify(productRepository).findAll(any(Pageable.class));
        verify(productMapper).toResponse(sampleProduct);
    }

    @Test
    @DisplayName("Should search products by term")
    void shouldSearchProducts() {
        // Given
        String searchTerm = "laptop";
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        
        when(productRepository.searchProducts(eq(searchTerm), any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        // When
        PageResponse<ProductResponse> result = productService.searchProducts(searchTerm, 0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).searchProducts(eq(searchTerm), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void shouldGetProductByIdSuccessfully() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        // When
        ProductResponse result = productService.getProductById(productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getSku()).isEqualTo("LAPTOP-001");
        verify(productRepository).findById(productId);
        verify(productMapper).toResponse(sampleProduct);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found by ID")
    void shouldThrowExceptionWhenProductNotFoundById() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id");
        
        verify(productRepository).findById(productId);
        verify(productMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should get product by SKU successfully")
    void shouldGetProductBySkuSuccessfully() {
        // Given
        String sku = "LAPTOP-001";
        when(productRepository.findBySku(sku)).thenReturn(Optional.of(sampleProduct));
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        // When
        ProductResponse result = productService.getProductBySku(sku);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo(sku);
        verify(productRepository).findBySku(sku);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found by SKU")
    void shouldThrowExceptionWhenProductNotFoundBySku() {
        // Given
        String sku = "NON-EXISTENT";
        when(productRepository.findBySku(sku)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductBySku(sku))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with SKU");
        
        verify(productRepository).findBySku(sku);
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        // Given
        when(productRepository.existsBySku(createRequest.getSku())).thenReturn(false);
        when(productMapper.toEntity(createRequest)).thenReturn(sampleProduct);
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        // When
        ProductResponse result = productService.createProduct(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("LAPTOP-001");
        verify(productRepository).existsBySku(createRequest.getSku());
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("Should throw ProductAlreadyExistsException when SKU already exists")
    void shouldThrowExceptionWhenSkuAlreadyExists() {
        // Given
        when(productRepository.existsBySku(createRequest.getSku())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessageContaining("Product already exists with SKU");
        
        verify(productRepository).existsBySku(createRequest.getSku());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);
        doNothing().when(productMapper).updateEntity(sampleProduct, updateRequest);

        // When
        ProductResponse result = productService.updateProduct(productId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findById(productId);
        verify(productMapper).updateEntity(sampleProduct, updateRequest);
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when updating non-existent product")
    void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(productId, updateRequest))
                .isInstanceOf(ProductNotFoundException.class);
        
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update product stock successfully")
    void shouldUpdateProductStockSuccessfully() {
        // Given
        UpdateStockRequest stockRequest = new UpdateStockRequest(25);
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        // When
        ProductResponse result = productService.updateProductStock(productId, stockRequest);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findById(productId);
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("Should delete product successfully")
    void shouldDeleteProductSuccessfully() {
        // Given
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when deleting non-existent product")
    void shouldThrowExceptionWhenDeletingNonExistentProduct() {
        // Given
        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(ProductNotFoundException.class);
        
        verify(productRepository).existsById(productId);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should get products by category")
    void shouldGetProductsByCategory() {
        // Given
        String category = "Electronics";
        Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending());
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        
        when(productRepository.findByCategory(eq(category), any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        // When
        PageResponse<ProductResponse> result = productService.getProductsByCategory(category, 0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findByCategory(eq(category), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get products by status")
    void shouldGetProductsByStatus() {
        // Given
        ProductStatus status = ProductStatus.ACTIVE;
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        
        when(productRepository.findByStatus(eq(status), any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        // When
        PageResponse<ProductResponse> result = productService.getProductsByStatus(status, 0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findByStatus(eq(status), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get active products")
    void shouldGetActiveProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        
        when(productRepository.findActiveProducts(any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

        // When
        PageResponse<ProductResponse> result = productService.getActiveProducts(0, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(ProductStatus.ACTIVE);
        verify(productRepository).findActiveProducts(any(Pageable.class));
    }
}
