package com.bitvelocity.product.api;

import com.bitvelocity.product.domain.Product;
import com.bitvelocity.product.repo.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Product> list() {
        return repo.findAll();
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductDto dto) {
        Product p = new Product();
        p.setSku(dto.sku()); p.setName(dto.name()); p.setDescription(dto.description()); p.setBasePrice(dto.basePrice());
        Product saved = repo.save(p);
        return ResponseEntity.created(URI.create("/api/v1/products/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> find(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}