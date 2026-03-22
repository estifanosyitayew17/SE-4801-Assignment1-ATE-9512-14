// ID: ATE/9512/14
package com.shopwave.config;

import com.shopwave.model.Category;
import com.shopwave.model.Product;
import com.shopwave.repository.CategoryRepository;
import com.shopwave.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (categoryRepository.count() == 0) {
            log.info("Initializing sample data...");

            // Create categories
            Category electronics = Category.builder()
                    .name("Electronics")
                    .description("Gadgets, devices, and electronic equipment")
                    .build();

            Category clothing = Category.builder()
                    .name("Clothing")
                    .description("Fashion apparel and accessories")
                    .build();

            Category books = Category.builder()
                    .name("Books")
                    .description("Printed and digital books")
                    .build();

            categoryRepository.saveAll(Arrays.asList(electronics, clothing, books));
            log.info("Categories created: Electronics, Clothing, Books");

            // Create products
            Product gamingLaptop = Product.builder()
                    .name("Gaming Laptop")
                    .description("High-performance gaming laptop with RTX 4080")
                    .price(new BigDecimal("1299.99"))
                    .stock(50)
                    .category(electronics)
                    .build();

            Product smartphone = Product.builder()
                    .name("Smartphone")
                    .description("Latest model with 5G and 128GB storage")
                    .price(new BigDecimal("699.99"))
                    .stock(100)
                    .category(electronics)
                    .build();

            Product headphones = Product.builder()
                    .name("Wireless Headphones")
                    .description("Noise-cancelling Bluetooth headphones")
                    .price(new BigDecimal("199.99"))
                    .stock(75)
                    .category(electronics)
                    .build();

            Product tShirt = Product.builder()
                    .name("Cotton T-Shirt")
                    .description("100% cotton comfortable t-shirt")
                    .price(new BigDecimal("19.99"))
                    .stock(200)
                    .category(clothing)
                    .build();

            Product jeans = Product.builder()
                    .name("Jeans")
                    .description("Classic fit denim jeans")
                    .price(new BigDecimal("49.99"))
                    .stock(150)
                    .category(clothing)
                    .build();

            Product javaBook = Product.builder()
                    .name("Java Programming Book")
                    .description("Comprehensive guide to Java programming")
                    .price(new BigDecimal("59.99"))
                    .stock(30)
                    .category(books)
                    .build();

            productRepository.saveAll(Arrays.asList(
                    gamingLaptop, smartphone, headphones, tShirt, jeans, javaBook));

            log.info("Sample data initialized successfully!");
        } else {
            log.info("Database already contains data, skipping initialization.");
        }
    }
}