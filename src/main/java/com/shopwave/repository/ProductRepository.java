// ID:ATE/9512/14
package com.shopwave.repository;

import com.shopwave.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by category ID
    List<Product> findByCategoryId(Long categoryId);

    // Find products with price less than or equal to max price
    List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);

    // Find products by name containing keyword (case insensitive)
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // Find the most expensive product
    Optional<Product> findTopByOrderByPriceDesc();

    // Additional useful methods
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    boolean existsById(Long id);
}