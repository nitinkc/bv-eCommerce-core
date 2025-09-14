package com.bitvelocity.product;

import com.bitvelocity.product.domain.Product;
import com.bitvelocity.product.repo.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class ProductRepositoryIT {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bitvelocity")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    ProductRepository repo;

    @Test
    void saveAndFind() {
        Product p = new Product();
        p.setSku("SKU-1");
        p.setName("Widget");
        p.setDescription("Demo");
        p.setBasePrice(9.99);
        Product saved = repo.save(p);
        assertThat(repo.findById(saved.getId())).isPresent();
    }
}