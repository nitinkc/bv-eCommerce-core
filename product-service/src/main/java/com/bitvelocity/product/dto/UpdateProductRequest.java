package com.bitvelocity.product.dto;

import com.bitvelocity.product.domain.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://)?.*\\.(jpg|jpeg|png|gif|webp)$", 
             message = "Image URL must be a valid image URL", 
             flags = Pattern.Flag.CASE_INSENSITIVE)
    private String imageUrl;

    private ProductStatus status;
}
