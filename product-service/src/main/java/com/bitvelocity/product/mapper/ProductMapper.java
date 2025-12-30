package com.bitvelocity.product.mapper;

import com.bitvelocity.product.domain.Product;
import com.bitvelocity.product.dto.CreateProductRequest;
import com.bitvelocity.product.dto.ProductResponse;
import com.bitvelocity.product.dto.UpdateProductRequest;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request) {
        return Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .status(request.getStatus())
                .build();
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .build();
    }

    public void updateEntity(Product product, UpdateProductRequest request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
    }
}
