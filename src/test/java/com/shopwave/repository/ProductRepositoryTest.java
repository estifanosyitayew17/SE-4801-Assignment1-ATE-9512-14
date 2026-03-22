// ID: ATE/9512/14
package com.shopwave.repository;

import com.shopwave.model.Category;
import com.shopwave.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category testCategory;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        // Create and save category
        testCategory = Category.builder()
                .name("Test Electronics")
                .description("Test category for electronics")
                .build();
        testCategory = categoryRepository.save(testCategory);

        // Create products
        product1 = Product.builder()
                .name("Gaming Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("1299.99"))
                .stock(50)
                .category(testCategory)
                .build();

        product2 = Product.builder()
                .name("Office Laptop")
                .description("Business laptop")
                .price(new BigDecimal("899.99"))
                .stock(30)
                .category(testCategory)
                .build();

        product3 = Product.builder()
                .name("Wireless Mouse")
                .description("Bluetooth mouse")
                .price(new BigDecimal("49.99"))
                .stock(100)
                .category(testCategory)
                .build();

        productRepository.saveAll(List.of(product1, product2, product3));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingProducts() {
        // When
        List<Product> results = productRepository.findByNameContainingIgnoreCase("laptop");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Product::getName)
                .containsExactlyInAnyOrder("Gaming Laptop", "Office Laptop");
    }

    @Test
    void findByNameContainingIgnoreCase_CaseInsensitive_ShouldWork() {
        // When
        List<Product> results = productRepository.findByNameContainingIgnoreCase("LAPTOP");

        // Then
        assertThat(results).hasSize(2);
    }

    @Test
    void findByNameContainingIgnoreCase_NoMatch_ShouldReturnEmptyList() {
        // When
        List<Product> results = productRepository.findByNameContainingIgnoreCase("smartphone");

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findByPriceLessThanEqual_ShouldReturnProductsUnderPrice() {
        // When
        List<Product> results = productRepository.findByPriceLessThanEqual(new BigDecimal("900.00"));

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Product::getName)
                .containsExactlyInAnyOrder("Office Laptop", "Wireless Mouse");
        assertThat(results).allMatch(p -> p.getPrice().compareTo(new BigDecimal("900.00")) <= 0);
    }

    @Test
    void findByPriceLessThanEqual_NoProducts_ShouldReturnEmptyList() {
        // When
        List<Product> results = productRepository.findByPriceLessThanEqual(new BigDecimal("10.00"));

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findByCategoryId_ShouldReturnProductsInCategory() {
        // When
        List<Product> results = productRepository.findByCategoryId(testCategory.getId());

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).allMatch(p -> p.getCategory().getId().equals(testCategory.getId()));
    }

    @Test
    void findTopByOrderByPriceDesc_ShouldReturnMostExpensiveProduct() {
        // When
        Optional<Product> result = productRepository.findTopByOrderByPriceDesc();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Gaming Laptop");
        assertThat(result.get().getPrice()).isEqualTo(new BigDecimal("1299.99"));
    }

    @Test
    void findByNameContainingIgnoreCaseAndPriceLessThanEqual_ShouldCombineFilters() {
        // When - Search for laptops under $1000
        List<Product> results = productRepository.findByNameContainingIgnoreCase("laptop")
                .stream()
                .filter(p -> p.getPrice().compareTo(new BigDecimal("1000.00")) <= 0)
                .toList();

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Office Laptop");
    }
}