package com.ecommerce.product.variation;

import com.ecommerce.product.category.Category;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
        VariationController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class VariationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VariationService variationService;

    @Test
    void VariationController_CreateVariation_Success() throws Exception {
        Integer categoryId = 1;

        Variation variation = Variation.builder()
                .id(1)
                .name("Size")
                .category(Category.builder().id(categoryId).build())
                .variationOptions(new ArrayList<>())
                .build();

        VariationRequest variationRequest = VariationRequest.builder()
                .categoryId(categoryId)
                .name("Size")
                .build();

        when(variationService.createVariation(any(VariationRequest.class))).thenReturn(variation);

        mockMvc.perform(post("/variation")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(variationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Size"));
    }

    @Test
    void VariationController_GetAllVariation_Success() throws Exception {

        VariationResponseCatName variation1 = VariationResponseCatName.builder()
                .id(1)
                .name("Size")
                .categoryName("Shirt")
                .variationOptions(new ArrayList<>())
                .build();

        VariationResponseCatName variation2 = VariationResponseCatName.builder()
                .id(2)
                .name("Colour")
                .categoryName("Shirt")
                .variationOptions(new ArrayList<>())
                .build();

        List<VariationResponseCatName> variations = List.of(variation1, variation2);

        when(variationService.getAllVariation()).thenReturn(variations);

        mockMvc.perform(get("/variation")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Size"))
                .andExpect(jsonPath("$[0].categoryName").value("Shirt"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Colour"))
                .andExpect(jsonPath("$[1].categoryName").value("Shirt"));
    }

    @Test
    void VariationController_GetVariationById_Success() throws Exception {
        Variation variation = Variation.builder()
                .id(1)
                .name("Size")
                .category(Category.builder().id(1).build())
                .variationOptions(new ArrayList<>())
                .build();

        when(variationService.getVariationById(eq(variation.getId()))).thenReturn(variation);

        mockMvc.perform(get("/variation/{variationId}", variation.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Size"));
    }

    @Test
    void VariationController_GetVariationByCategoryId_Success() throws Exception {
        Integer categoryId = 1;

        Variation variation1 = Variation.builder()
                .id(1)
                .name("Size")
                .category(Category.builder().id(categoryId).build())
                .variationOptions(new ArrayList<>())
                .build();

        Variation variation2 = Variation.builder()
                .id(2)
                .name("Colour")
                .category(Category.builder().id(categoryId).build())
                .variationOptions(new ArrayList<>())
                .build();

        List<Variation> variations = List.of(variation1, variation2);

        when(variationService.getVariationByCategoryId(eq(categoryId))).thenReturn(variations);

        mockMvc.perform(get("/variation/byCategory/{categoryId}", categoryId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Size"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Colour"));
    }

    @Test
    void VariationController_UpdatedVariation_Success() throws Exception {
        Integer categoryId = 1;
        Integer variationId = 1;

        Variation variation = Variation.builder()
                .id(variationId)
                .name("Colour")
                .category(Category.builder().id(categoryId).build())
                .variationOptions(new ArrayList<>())
                .build();

        VariationRequest variationRequest = VariationRequest.builder()
                .name("Colour")
                .categoryId(categoryId)
                .build();

        when(variationService.updateVariation(eq(variationId), any(VariationRequest.class))).thenReturn(variation);

        mockMvc.perform(put("/variation/{variationId}", variationId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(variationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(variationId))
                .andExpect(jsonPath("$.name").value("Colour"));
    }

    @Test
    void VariationController_DeleteVariation_Success() throws Exception {
        Integer variationId = 1;

        doNothing().when(variationService).deleteVariation(variationId);


        mockMvc.perform(delete("/variation/{variationId}", variationId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Variation has been successfully deleted!"));
    }
}
