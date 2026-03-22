// ID: ATE/9512/14
package com.shopwave.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.dto.UpdateStockRequest;
import com.shopwave.exception.GlobalExceptionHandler;
import com.shopwave.exception.ProductNotFoundException;
import com.shopwave.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductDTO sampleProductDTO;
    private CreateProductRequest createRequest;
    private UpdateStockRequest updateStockRequest;

    @BeforeEach
    void setUp() {
        sampleProductDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(50)
                .categoryId(1L)
                .categoryName("Electronics")
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = CreateProductRequest.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("149.99"))
                .stock(30)
                .categoryId(1L)
                .build();

        updateStockRequest = UpdateStockRequest.builder()
                .delta(-5)
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnPaginatedProducts() throws Exception {
        // Given
        Page<ProductDTO> productPage = new PageImpl<>(
                List.of(sampleProductDTO),
                PageRequest.of(0, 10),
                1
        );

        when(productService.getAllProducts(any())).thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService).getAllProducts(any());
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() throws Exception {
        // Given
        when(productService.getProductById(1L)).thenReturn(sampleProductDTO);

        // When & Then
        mockMvc.perform(get("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));

        verify(productService).getProductById(1L);
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldReturn404WithErrorJson() throws Exception {
        // Given
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/products/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product with ID 999 not found"))
                .andExpect(jsonPath("$.path").value("/api/products/999"));

        verify(productService).getProductById(999L);
    }

    @Test
    void createProduct_WithValidRequest_ShouldReturn201() throws Exception {
        // Given
        ProductDTO createdProduct = ProductDTO.builder()
                .id(2L)
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("149.99"))
                .stock(30)
                .categoryId(1L)
                .categoryName("Electronics")
                .createdAt(LocalDateTime.now())
                .build();

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(createdProduct);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(149.99));

        verify(productService).createProduct(any(CreateProductRequest.class));
    }

    @Test
    void createProduct_WithInvalidData_ShouldReturn400() throws Exception {
        // Given - Invalid request (empty name)
        CreateProductRequest invalidRequest = CreateProductRequest.builder()
                .name("")  // Empty name should fail validation
                .description("Test")
                .price(new BigDecimal("-10"))  // Negative price
                .stock(-5)  // Negative stock
                .categoryId(null)  // Null category ID
                .build();

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists())
                .andExpect(jsonPath("$.validationErrors.stock").exists());

        verify(productService, never()).createProduct(any());
    }

    @Test
    void updateStock_WithValidDelta_ShouldReturn200() throws Exception {
        // Given
        ProductDTO updatedProduct = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stock(45)  // Reduced by 5
                .categoryId(1L)
                .categoryName("Electronics")
                .build();

        when(productService.updateStock(eq(1L), eq(-5))).thenReturn(updatedProduct);

        // When & Then
        mockMvc.perform(patch("/api/products/{id}/stock", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStockRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stock").value(45));

        verify(productService).updateStock(1L, -5);
    }

    @Test
    void searchProducts_WithKeyword_ShouldReturnFilteredList() throws Exception {
        // Given
        List<ProductDTO> products = List.of(sampleProductDTO);
        when(productService.searchProducts(eq("Test"), eq(null))).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "Test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).searchProducts("Test", null);
    }

    @Test
    void getMostExpensiveProduct_ShouldReturnProduct() throws Exception {
        // Given
        when(productService.getMostExpensiveProduct()).thenReturn(sampleProductDTO);

        // When & Then
        mockMvc.perform(get("/api/products/most-expensive")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).getMostExpensiveProduct();
    }
}