package com.bitvelocity.product.repository;

import com.bitvelocity.product.domain.Product;
import com.bitvelocity.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Check if product exists by SKU
     */
    boolean existsBySku(String sku);

    /**
     * Find all products by category with pagination
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Find all products by status with pagination
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    /**
     * Search products by name or description (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products by category and status
     */
    Page<Product> findByCategoryAndStatus(String category, ProductStatus status, Pageable pageable);

    /**
     * Find all active products
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    Page<Product> findActiveProducts(Pageable pageable);
}
