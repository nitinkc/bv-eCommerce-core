package com.bitvelocity.product.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProductDto(
        Long id,
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @Positive double basePrice
) {}