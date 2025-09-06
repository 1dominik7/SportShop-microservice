package com.ecommerce.product.variation.variationOption;

import com.ecommerce.product.variation.Variation;
import com.ecommerce.product.variation.VariationOption;
import com.ecommerce.product.variation.VariationOptionController;
import com.ecommerce.product.variation.VariationOptionRequest;
import com.ecommerce.product.variation.VariationOptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
        VariationOptionController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class VariationOptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VariationOptionService variationOptionService;

    @Test
    void VariationOptionController_CreateVariationOption_Success() throws Exception {
        Integer variationId = 1;
        Variation variation = new Variation();
        variation.setId(variationId);

        VariationOptionRequest request = new VariationOptionRequest();
        request.setVariationId(variationId);
        request.setValue("Shirt");

        VariationOption response = new VariationOption();
        response.setId(1);
        response.setValue("Shirt");
        response.setVariation(variation);

        when(variationOptionService.createVariationOption(any(VariationOptionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/variation-option")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.value").value("Shirt"));
    }

    @Test
    void VariationOptionController_GetAllVariationOptions_Success() throws Exception {
        String keycloakId = "keycloak-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        Integer variationId = 1;

        Variation variation = new Variation();
        variation.setId(variationId);

        VariationOption savedVariationOption1 = VariationOption.builder()
                .variation(null)
                .id(1)
                .value("Shirt1")
                .build();

        VariationOption savedVariationOption2 = VariationOption.builder()
                .variation(null)
                .id(2)
                .value("Shirt2")
                .build();

        List<VariationOption> mockOptions = List.of(savedVariationOption1, savedVariationOption2);

        when(variationOptionService.getAllVariationOptions()).thenReturn(mockOptions);

        mockMvc.perform(get("/variation-option")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].value").value("Shirt1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].value").value("Shirt2"));
    }


    @Test
    void VariationOptionController_GetVariationOptionById_Success() throws Exception {
        String keycloakId = "keycloak-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);

        Integer variationId = 1;

        Variation variation = new Variation();
        variation.setId(variationId);

        VariationOption savedVariationOption1 = VariationOption.builder()
                .variation(null)
                .id(1)
                .value("Shirt1")
                .build();


        when(variationOptionService.getVariationOptionById(1)).thenReturn(savedVariationOption1);

        mockMvc.perform(get("/variation-option/{variationOptionId}", savedVariationOption1.getId())
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.value").value("Shirt1"));
    }

    @Test
    void VariationOptionController_UpdateVariationOption_Success() throws Exception {
        String keycloakId = "keycloak-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);
        Integer variationOptionId = 1;

        VariationOptionRequest variationOptionRequest = new VariationOptionRequest();
        variationOptionRequest.setValue("Updated Shirt");

        VariationOption updatedVariationOption = new VariationOption();
        updatedVariationOption.setId(variationOptionId);
        updatedVariationOption.setValue("Updated Shirt");

        when(variationOptionService.updateVariationOption(eq(1), any(VariationOptionRequest.class)))
                .thenReturn(updatedVariationOption);

        mockMvc.perform(put("/variation-option/{variationOptionId}", variationOptionId)
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(variationOptionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.value").value("Updated Shirt"));
    }

    @Test
    void VariationOptionController_DeleteVariationOption_Success() throws Exception {
        String keycloakId = "keycloak-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(keycloakId);
        Integer variationOptionId = 1;

        doNothing().when(variationOptionService).deleteVariationOption(variationOptionId);

        mockMvc.perform(delete("/variation-option/{variationOptionId}", variationOptionId)
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isOk())
                .andExpect(content().string("Variation Option has been successfully deleted!"));
    }
}
