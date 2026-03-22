// ID: ATE/9512/14
package com.shopwave.service;

import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.dto.UpdateStockRequest;
import com.shopwave.exception.ProductNotFoundException;
import com.shopwave.mapper.ProductMapper;
import com.shopwave.model.Category;
import com.shopwave.model.Product;
import com.shopwave.repository.CategoryRepository;
import com.shopwave.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    /**
     * Create a new product
     */
    public ProductDTO createProduct(CreateProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        // Find category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category with ID " + request.getCategoryId() + " not found"));

        // Map request to entity
        Product product = productMapper.toEntity(request, category);

        // Validate price and stock
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        // Save product
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toDto(savedProduct);
    }

    /**
     * Get all products with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pagination: {}", pageable);
        return productRepository.findAll(pageable)
                .map(productMapper::toDto);
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return productMapper.toDto(product);
    }

    /**
     * Search products by keyword and/or max price
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword, BigDecimal maxPrice) {
        log.debug("Searching products - keyword: {}, maxPrice: {}", keyword, maxPrice);

        List<Product> products;

        if (keyword != null && !keyword.trim().isEmpty() && maxPrice != null) {
            // Both filters applied
            products = productRepository.findByNameContainingIgnoreCase(keyword)
                    .stream()
                    .filter(p -> p.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // Only keyword filter
            products = productRepository.findByNameContainingIgnoreCase(keyword);
        } else if (maxPrice != null) {
            // Only price filter
            products = productRepository.findByPriceLessThanEqual(maxPrice);
        } else {
            // No filters - return all
            products = productRepository.findAll();
        }

        return products.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Update product stock (delta can be positive or negative)
     */
    public ProductDTO updateStock(Long id, Integer delta) {
        log.info("Updating stock for product ID: {}, delta: {}", id, delta);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        int newStock = product.getStock() + delta;

        if (newStock < 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient stock! Current stock: %d, Requested reduction: %d",
                            product.getStock(), -delta));
        }

        product.setStock(newStock);
        Product updatedProduct = productRepository.save(product);
        log.info("Stock updated successfully. New stock: {}", newStock);

        return productMapper.toDto(updatedProduct);
    }

    /**
     * Get the most expensive product
     */
    @Transactional(readOnly = true)
    public ProductDTO getMostExpensiveProduct() {
        log.debug("Fetching most expensive product");

        Product product = productRepository.findTopByOrderByPriceDesc()
                .orElseThrow(() -> new ProductNotFoundException("No products found"));

        return productMapper.toDto(product);
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        log.debug("Fetching products by category ID: {}", categoryId);

        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category with ID " + categoryId + " not found");
        }

        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }
}