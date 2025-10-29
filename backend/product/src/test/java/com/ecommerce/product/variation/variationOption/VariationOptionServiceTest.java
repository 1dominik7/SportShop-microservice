package com.ecommerce.product.variation.variationOption;


import com.ecommerce.product.variation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VariationOptionServiceTest {

    @Mock
    private VariationOptionRepository variationOptionRepository;

    @Mock
    private VariationRepository variationRepository;

    @InjectMocks
    private VariationOptionService variationOptionService;

    @Mock
    private Jwt jwt;

    @Test
    void VariationOptionService_createVariationOption_Success() {
        Integer variationId = 1;

        Variation variation = new Variation();
        variation.setId(variationId);

        VariationOptionRequest variationOptionRequest = VariationOptionRequest.builder()
                .variationId(variationId)
                .value("Shirt")
                .build();

        VariationOption savedVariationOption = VariationOption.builder()
                .variation(variation)
                .id(1)
                .value("Shirt")
                .build();

        when(variationRepository.findById(variationId)).thenReturn(Optional.of(variation));
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(savedVariationOption);

        VariationOption variationOption = variationOptionService.createVariationOption(variationOptionRequest);

        assertNotNull(variationOption);
        assertEquals(variationOptionRequest.getVariationId(), savedVariationOption.getVariation().getId());
        assertEquals(variationOptionRequest.getValue(), savedVariationOption.getValue());

        verify(variationRepository).findById(variationId);
        verify(variationOptionRepository).save(any(VariationOption.class));
    }

    @Test
    void VariationOptionService_createVariationOption_WhenVariationIdIsNull() {
        Integer variationId = null;

        Variation variation = new Variation();
        variation.setId(variationId);

        VariationOptionRequest variationOptionRequest = VariationOptionRequest.builder()
                .variationId(variationId)
                .value("Shirt")
                .build();

        VariationOption savedVariationOption = VariationOption.builder()
                .variation(null)
                .id(1)
                .value("Shirt")
                .build();

        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(savedVariationOption);

        VariationOption variationOption = variationOptionService.createVariationOption(variationOptionRequest);

        assertNotNull(variationOption);
        assertEquals(variationOptionRequest.getValue(), variationOption.getValue());
        assertNull(variationOption.getVariation());

        verify(variationOptionRepository).save(any(VariationOption.class));
        verifyNoInteractions(variationRepository);
    }

    @Test
    void VariationOptionService_GetAllVariationOptions_Success() {

        Integer variationId = 1;

        Variation variation = new Variation();
        variation.setId(variationId);

        VariationOption variationOption1 = VariationOption.builder()
                .variation(variation)
                .id(1)
                .value("Shirt")
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .variation(variation)
                .id(1)
                .value("Shirt")
                .build();

        when(variationOptionRepository.findAll()).thenReturn(List.of(variationOption1, variationOption2));

        List<VariationOptionWithVariationResponse> variationOptions = variationOptionService.getAllVariationOptions();

        assertNotNull(variationOptions);
        assertEquals(2, variationOptions.size());

        verify(variationOptionRepository).findAll();
    }

    @Test
    void VariationOptionService_GetVariationOptionById_Success() {
        Integer variationId = 1;

        Variation variation = new Variation();
        variation.setId(variationId);

        VariationOption variationOption = VariationOption.builder()
                .variation(variation)
                .id(1)
                .value("Shirt")
                .build();

        when(variationOptionRepository.findById(eq(variationOption.getId()))).thenReturn(Optional.of(variationOption));

        VariationOption result = variationOptionService.getVariationOptionById(variationOption.getId());

        assertNotNull(result);
        assertEquals(variationOption.getId(), result.getId());
        assertEquals(variationOption.getValue(), result.getValue());
        assertEquals(variationOption.getVariation().getId(), result.getVariation().getId());

        verify(variationOptionRepository).findById(variationOption.getId());
    }

    @Test
    void VariationOptionService_UpdateVariationOption_Success() {
        Integer variationId = 1;
        Integer variationOptionId = 1;

        Variation variation = new Variation();
        variation.setId(variationId);

        VariationOption existingVariationOption = VariationOption.builder()
                .id(variationOptionId)
                .value("Shirt")
                .variation(null)
                .build();

        VariationOptionRequest variationOptionUpdateRequest = VariationOptionRequest.builder()
                .variationId(variationId)
                .value("Update")
                .build();

        VariationOption updatedVariationOption = VariationOption.builder()
                .id(variationOptionId)
                .variation(variation)
                .value("Update")
                .build();

        when(variationOptionRepository.findById(variationOptionId)).thenReturn(Optional.of(existingVariationOption));
        when(variationRepository.findById(eq(variationId)))
                .thenReturn(Optional.of(variation));
        when(variationOptionRepository.save(any(VariationOption.class))).thenReturn(updatedVariationOption);

        VariationOption result = variationOptionService.updateVariationOption(variationOptionId, variationOptionUpdateRequest);

        assertNotNull(result);
        assertEquals(variationOptionId, result.getId());
        assertEquals("Update", result.getValue());
        assertEquals(variationId, result.getVariation().getId());

        verify(variationOptionRepository).findById(variationOptionId);
        verify(variationRepository).findById(variationId);
        verify(variationOptionRepository).save(any(VariationOption.class));
    }

    @Test
    void VariationOptionService_DeleteVariationOption_Success() {
        Integer variationId = 1;
        Integer variationOptionId = 1;

        Variation variation = new Variation();
        variation.setId(variationId);

        VariationOption variationOption = VariationOption.builder()
                .id(variationOptionId)
                .value("Shirt")
                .variation(null)
                .build();

        when(variationOptionRepository.findById(variationOptionId)).thenReturn(Optional.of(variationOption));

        variationOptionService.deleteVariationOption(variationOptionId);

        verify(variationOptionRepository).findById(variationOptionId);
        verify(variationOptionRepository).delete(variationOption);
    }
    }
