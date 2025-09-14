package com.bitvelocity.product.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="products")
public class Product {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable=false, unique=true)
  private String sku;
  @Column(nullable=false)
  private String name;
  private String description;
  private double basePrice;
  private Instant createdAt;
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    createdAt = updatedAt = Instant.now();
  }
  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  // getters/setters omitted for brevity
  public Long getId() { return id; }
  public String getSku() { return sku; }
  public void setSku(String sku) { this.sku = sku; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public double getBasePrice() { return basePrice; }
  public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
}