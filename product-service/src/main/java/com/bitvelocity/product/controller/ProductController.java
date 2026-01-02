package com.bitvelocity.product.controller;

import com.bitvelocity.product.domain.ProductStatus;
import com.bitvelocity.product.dto.*;
import com.bitvelocity.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "APIs for managing products in the eCommerce platform")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get all products", description = "Retrieve all products with pagination and sorting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products",
                     content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                     content = @Content(schema = @Schema(implementation = com.bitvelocity.product.exception.ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("GET /products - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        PageResponse<ProductResponse> response = productService.getAllProducts(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search products", description = "Search products by name or description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved",
                     content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @Parameter(description = "Search term") @RequestParam String query,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /products/search - query: {}", query);
        PageResponse<ProductResponse> response = productService.searchProducts(query, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get products by category", description = "Retrieve products filtered by category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved",
                     content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByCategory(
            @Parameter(description = "Product category") @PathVariable String category,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /products/category/{} - page: {}, size: {}", category, page, size);
        PageResponse<ProductResponse> response = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get products by status", description = "Retrieve products filtered by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved",
                     content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByStatus(
            @Parameter(description = "Product status") @PathVariable ProductStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /products/status/{} - page: {}, size: {}", status, page, size);
        PageResponse<ProductResponse> response = productService.getProductsByStatus(status, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get active products", description = "Retrieve all active products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active products retrieved",
                     content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @GetMapping("/active")
    public ResponseEntity<PageResponse<ProductResponse>> getActiveProducts(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /products/active - page: {}, size: {}", page, size);
        PageResponse<ProductResponse> response = productService.getActiveProducts(page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found",
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = com.bitvelocity.product.exception.ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product UUID") @PathVariable UUID id) {
        
        log.info("GET /products/{}", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get product by SKU", description = "Retrieve a specific product by its SKU")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found",
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = com.bitvelocity.product.exception.ErrorResponse.class)))
    })
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(
            @Parameter(description = "Product SKU") @PathVariable String sku) {
        
        log.info("GET /products/sku/{}", sku);
        ProductResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create product", description = "Create a new product (requires ADMIN or VENDOR role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully",
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
                     content = @Content(schema = @Schema(implementation = com.bitvelocity.product.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "409", description = "Product with SKU already exists",
                     content = @Content(schema = @Schema(implementation = com.bitvelocity.product.exception.ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("POST /products - User {} creating product with SKU: {}", 
                userDetails.getUsername(), request.getSku());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update product", description = "Update an existing product (requires ADMIN or VENDOR role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully",
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
                     content = @Content(schema = @Schema(implementation = com.bitvelocity.product.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = com.bitvelocity.product.exception.ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("PUT /products/{} - User {} updating product", id, userDetails.getUsername());
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update product stock", description = "Update stock quantity for a product (requires ADMIN or VENDOR role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock updated successfully",
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
                     content = @Content(schema = @Schema(implementation = com.bitvelocity.product.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = com.bitvelocity.product.exception.ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateProductStock(
            @Parameter(description = "Product UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateStockRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("PATCH /products/{}/stock - User {} updating stock to {}", 
                id, userDetails.getUsername(), request.getStockQuantity());
        ProductResponse response = productService.updateProductStock(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete product", description = "Delete a product by ID (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only ADMIN can delete products"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = com.bitvelocity.product.exception.ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product UUID") @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("DELETE /products/{} - User {} deleting product", id, userDetails.getUsername());
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
