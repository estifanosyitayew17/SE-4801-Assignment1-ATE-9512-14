// ID: ATE/9512/14
package com.shopwave.service;

import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.exception.ProductNotFoundException;
import com.shopwave.mapper.ProductMapper;
import com.shopwave.model.Category;
import com.shopwave.model.Product;
import com.shopwave.repository.CategoryRepository;
import com.shopwave.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private ProductMapper productMapper;
    private Category testCategory;
    private Product testProduct;
    private CreateProductRequest validRequest;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
        productService = new ProductService(productRepository, categoryRepository, productMapper);

        // Setup test data
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .category(testCategory)
                .build();

        validRequest = CreateProductRequest.builder()
                .name("Test Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .categoryId(1L)
                .build();
    }

    @Test
    void createProduct_HappyPath_ShouldReturnProductDTO() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductDTO result = productService.createProduct(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Laptop");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("999.99"));
        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getCategoryName()).isEqualTo("Electronics");

        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_CategoryNotFound_ShouldThrowIllegalArgumentException() {
        // Given
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        validRequest.setCategoryId(99L);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category with ID 99 not found");

        verify(categoryRepository).findById(99L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_InvalidPrice_ShouldThrowIllegalArgumentException() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        validRequest.setPrice(BigDecimal.ZERO);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Price must be greater than 0");

        verify(categoryRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_HappyPath_ShouldReturnProductDTO() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        ProductDTO result = productService.getProductById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Laptop");
        assertThat(result.getCategoryName()).isEqualTo("Electronics");

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_ProductNotFound_ShouldThrowProductNotFoundException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product with ID 999 not found");

        verify(productRepository).findById(999L);
    }

    @Test
    void getAllProducts_ShouldReturnPageOfProductDTO() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // When
        Page<ProductDTO> result = productService.getAllProducts(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Laptop");

        verify(productRepository).findAll(pageable);
    }

    @Test
    void updateStock_HappyPath_ShouldReturnUpdatedProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductDTO result = productService.updateStock(1L, -5);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStock()).isEqualTo(45); // 50 - 5 = 45

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateStock_NegativeStock_ShouldThrowIllegalArgumentException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        assertThatThrownBy(() -> productService.updateStock(1L, -100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void searchProducts_WithKeyword_ShouldReturnFilteredProducts() {
        // Given
        List<Product> products = List.of(testProduct);
        when(productRepository.findByNameContainingIgnoreCase("Laptop")).thenReturn(products);

        // When
        List<ProductDTO> result = productService.searchProducts("Laptop", null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Laptop");

        verify(productRepository).findByNameContainingIgnoreCase("Laptop");
    }

    @Test
    void getMostExpensiveProduct_ShouldReturnProduct() {
        // Given
        when(productRepository.findTopByOrderByPriceDesc()).thenReturn(Optional.of(testProduct));

        // When
        ProductDTO result = productService.getMostExpensiveProduct();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(productRepository).findTopByOrderByPriceDesc();
    }
}