// ID: ATE/9512/14
package com.shopwave.controller;

import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.dto.UpdateStockRequest;
import com.shopwave.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products?page=0&size=10
     * Get all products with pagination
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /api/products - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ProductDTO> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/{id}
     * Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);

        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * POST /api/products
     * Create a new product
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("POST /api/products - creating product: {}", request.getName());

        ProductDTO createdProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * GET /api/products/search?keyword=&maxPrice=
     * Search products by keyword and/or max price
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal maxPrice) {

        log.info("GET /api/products/search - keyword: {}, maxPrice: {}", keyword, maxPrice);

        List<ProductDTO> products = productService.searchProducts(keyword, maxPrice);
        return ResponseEntity.ok(products);
    }

    /**
     * PATCH /api/products/{id}/stock
     * Update product stock
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDTO> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {

        log.info("PATCH /api/products/{}/stock - delta: {}", id, request.getDelta());

        ProductDTO updatedProduct = productService.updateStock(id, request.getDelta());
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Additional endpoint: Get most expensive product
     */
    @GetMapping("/most-expensive")
    public ResponseEntity<ProductDTO> getMostExpensiveProduct() {
        log.info("GET /api/products/most-expensive");

        ProductDTO product = productService.getMostExpensiveProduct();
        return ResponseEntity.ok(product);
    }

    /**
     * Additional endpoint: Get products by category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        log.info("GET /api/products/category/{}", categoryId);

        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }
}