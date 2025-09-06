package com.ecommerce.product.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
        ProductController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void ProductController_CreateProduct_Success() throws Exception {
        ProductCreateResponse mockResponse = ProductCreateResponse.builder()
                .id(1)
                .productName("Shoes")
                .description("Sport Shoes")
                .build();

        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .productName("Shoes")
                .description("Sport Shoes")
                .build();

        when(productService.createProduct(any(ProductCreateRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(productCreateRequest))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Shoes"))
                .andExpect(jsonPath("$.description").value("Sport Shoes"));
    }

    @Test
    void ProductController_GetAllProducts_Success() throws Exception {

        ProductRequest productRequest = ProductRequest.builder()
                .productName("Test")
                .categoryId(1)
                .build();

        ProductResponse response = ProductResponse.builder()
                .content(List.of(productRequest))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(0L)
                .totalPages(0)
                .lastPage(true)
                .build();

        when(productService.getProducts(any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/products/searchByCategory")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("categoryId", "1")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "productName")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("Test"))
                .andExpect(jsonPath("$.content[0].categoryId").value("1"))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void ProductController_GetProductById_Success() throws Exception {
        ProductResponseGetById response = ProductResponseGetById.builder()
                .id(1)
                .productName("T-Shirt")
                .description("Cotton T-Shirt")
                .build();

        when(productService.getProductById(1)).thenReturn(response);

        mockMvc.perform(get("/products/{id}", response.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("T-Shirt"));
    }

    @Test
    void ProductController_GetTheNewestProducts_Success() throws Exception {
        ProductRequest product = ProductRequest.builder()
                .productName("Newest")
                .categoryId(1)
                .build();

        when(productService.getTheNewestProducts(5))
                .thenReturn(List.of(product));

        mockMvc.perform(get("/products/limit/5")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Newest"))
                .andExpect(jsonPath("$[0].categoryId").value("1"));
    }

    @Test
    void ProductController_UpdateProduct_Success() throws Exception {

        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .productName("Updated Shoes")
                .description("Updated Shoes")
                .build();

        ProductCreateResponse mockResponse = ProductCreateResponse.builder()
                .id(1)
                .productName("Updated Shoes")
                .description("Updated Shoes")
                .build();

        when(productService.updateProduct(any(ProductCreateRequest.class), eq(1)))
                .thenReturn(mockResponse);

        mockMvc.perform(put("/products/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(productCreateRequest))).andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Shoes"))
                .andExpect(jsonPath("$.description").value("Updated Shoes"));
    }

    @Test
    void ProductController_DeleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(1);

        mockMvc.perform(delete("/products/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Product has been deleted!"));
    }
}
