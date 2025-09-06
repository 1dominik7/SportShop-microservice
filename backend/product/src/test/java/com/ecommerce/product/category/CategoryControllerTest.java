package com.ecommerce.product.category;

import com.ecommerce.product.variation.Variation;
import com.ecommerce.product.variation.VariationController;
import com.ecommerce.product.variation.VariationRequest;
import com.ecommerce.product.variation.VariationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
        CategoryController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void CategoryController_CreateCategory_Success() throws Exception {

        Integer categoryId = 1;

        CategoryRequest categoryRequest = CategoryRequest.builder()
                .categoryName("Shirt")
                .parentCategoryId(1)
                .build();

        CategoryResponse category = CategoryResponse.builder()
                .id(categoryId)
                .categoryName("Shirt")
                .parentCategoryId(1)
                .build();

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(category);

        mockMvc.perform(post("/category/create")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("Shirt"))
                .andExpect(jsonPath("$.parentCategoryId").value(1));
    }

    @Test
    void CategoryController_GetAllCategories_Success() throws Exception {
        CategoryResponse category1 = CategoryResponse.builder()
                .id(1)
                .categoryName("Shirt")
                .parentCategoryId(1)
                .build();

        CategoryResponse category2 = CategoryResponse.builder()
                .id(2)
                .categoryName("Boots")
                .parentCategoryId(2)
                .build();

        List<CategoryResponse> categories = List.of(category1,category2);

        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/category/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value(category1.getCategoryName()))
                .andExpect(jsonPath("$[0].parentCategoryId").value(category1.getParentCategoryId()))
                .andExpect(jsonPath("$[1].categoryName").value(category2.getCategoryName()))
                .andExpect(jsonPath("$[1].parentCategoryId").value(category2.getParentCategoryId()));
    }

    @Test
    void CategoryController_FindCategoryById_Success() throws Exception {
        CategoryWithVariationResponse category1 = CategoryWithVariationResponse.builder()
                .id(1)
                .categoryName("Shirt")
                .parentCategoryId(1)
                .build();

        CategoryWithVariationResponse category2 = CategoryWithVariationResponse.builder()
                .id(2)
                .categoryName("Boots")
                .parentCategoryId(1)
                .build();

        List<Integer> ids = List.of(category1.getId(), category2.getId());
        List<CategoryWithVariationResponse> categories = List.of(category1, category2);

        when(categoryService.getCategoryById(eq(ids))).thenReturn(categories);

        mockMvc.perform(get("/category/byId")
                        .param("ids", "1", "2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(category1.getId()))
                .andExpect(jsonPath("$[0].categoryName").value(category1.getCategoryName()))
                .andExpect(jsonPath("$[0].parentCategoryId").value(category1.getParentCategoryId()))
                .andExpect(jsonPath("$[1].id").value(category2.getId()))
                .andExpect(jsonPath("$[1].categoryName").value(category2.getCategoryName()))
                .andExpect(jsonPath("$[1].parentCategoryId").value(category2.getParentCategoryId()));
    }

    @Test
    void CategoryController_UpdatedCategory_Success() throws Exception {
        Integer categoryId = 1;

        CategoryRequest categoryRequest = CategoryRequest.builder()
                .categoryName("Colour")
                .parentCategoryId(null)
                .build();

        Category updatedCategory = Category.builder()
                .id(categoryId)
                .categoryName("Colour")
                .build();

        when(categoryService.updateCategory(eq(categoryId), any(CategoryRequest.class))).thenReturn(updatedCategory);

        mockMvc.perform(put("/category/{categoryId}", categoryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(categoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.categoryName").value("Colour"));
    }

    @Test
    void CategoryController_DeleteCategory_Success() throws Exception {
        Integer categoryId = 1;

        doNothing().when(categoryService).deleteCategory(categoryId);


        mockMvc.perform(delete("/category/{categoryId}", categoryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Category has been successfully deleted!"));
    }
}
