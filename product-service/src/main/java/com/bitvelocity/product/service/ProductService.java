package com.bitvelocity.product.service;

import com.bitvelocity.product.domain.Product;
import com.bitvelocity.product.domain.ProductStatus;
import com.bitvelocity.product.dto.*;
import com.bitvelocity.product.exception.ProductAlreadyExistsException;
import com.bitvelocity.product.exception.ProductNotFoundException;
import com.bitvelocity.product.mapper.ProductMapper;
import com.bitvelocity.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Get all products with pagination and sorting
     */
    public PageResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        log.debug("Getting all products - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                  page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                    ? Sort.by(sortBy).ascending() 
                    : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findAll(pageable);
        
        return mapToPageResponse(productPage);
    }

    /**
     * Search products by term (name or description)
     */
    public PageResponse<ProductResponse> searchProducts(String searchTerm, int page, int size) {
        log.debug("Searching products with term: {}", searchTerm);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.searchProducts(searchTerm, pageable);
        
        return mapToPageResponse(productPage);
    }

    /**
     * Get products by category
     */
    public PageResponse<ProductResponse> getProductsByCategory(String category, int page, int size) {
        log.debug("Getting products by category: {}", category);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> productPage = productRepository.findByCategory(category, pageable);
        
        return mapToPageResponse(productPage);
    }

    /**
     * Get products by status
     */
    public PageResponse<ProductResponse> getProductsByStatus(ProductStatus status, int page, int size) {
        log.debug("Getting products by status: {}", status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findByStatus(status, pageable);
        
        return mapToPageResponse(productPage);
    }

    /**
     * Get active products only
     */
    public PageResponse<ProductResponse> getActiveProducts(int page, int size) {
        log.debug("Getting active products");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findActiveProducts(pageable);
        
        return mapToPageResponse(productPage);
    }

    /**
     * Get product by ID
     */
    public ProductResponse getProductById(UUID id) {
        log.debug("Getting product by id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        return productMapper.toResponse(product);
    }

    /**
     * Get product by SKU
     */
    public ProductResponse getProductBySku(String sku) {
        log.debug("Getting product by SKU: {}", sku);
        
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
        
        return productMapper.toResponse(product);
    }

    /**
     * Create new product
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());
        
        // Check if product with same SKU already exists
        if (productRepository.existsBySku(request.getSku())) {
            throw new ProductAlreadyExistsException(request.getSku());
        }
        
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        
        log.info("Product created successfully with id: {}", savedProduct.getId());
        return productMapper.toResponse(savedProduct);
    }

    /**
     * Update existing product
     */
    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Updating product with id: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        productMapper.updateEntity(product, request);
        Product updatedProduct = productRepository.save(product);
        
        log.info("Product updated successfully with id: {}", updatedProduct.getId());
        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Update product stock quantity
     */
    @Transactional
    public ProductResponse updateProductStock(UUID id, UpdateStockRequest request) {
        log.info("Updating stock for product id: {} to {}", id, request.getStockQuantity());
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        product.setStockQuantity(request.getStockQuantity());
        Product updatedProduct = productRepository.save(product);
        
        log.info("Stock updated successfully for product id: {}", updatedProduct.getId());
        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Delete product
     */
    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product with id: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        
        productRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }

    /**
     * Helper method to map Page<Product> to PageResponse<ProductResponse>
     */
    private PageResponse<ProductResponse> mapToPageResponse(Page<Product> productPage) {
        return PageResponse.<ProductResponse>builder()
                .content(productPage.getContent().stream()
                        .map(productMapper::toResponse)
                        .toList())
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }
}
