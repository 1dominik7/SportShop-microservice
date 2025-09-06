package com.ecommerce.product.variation;

import com.ecommerce.product.category.Category;
import com.ecommerce.product.category.CategoryRepository;
import com.ecommerce.product.exceptions.NotFoundException;
import com.ecommerce.product.variation.VariationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class VariationServiceTest {

    @Mock
    private VariationOptionRepository variationOptionRepository;

    @Mock
    private VariationRepository variationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private VariationService variationService;

    @Mock
    private Jwt jwt;

    @Test
    void VariationService_CreateVariation_Success() {
        Integer categoryId = 1;

        Variation variation = Variation.builder()
                .id(1)
                .name("Size")
                .category(Category.builder().id(categoryId).build())
                .variationOptions(new ArrayList<>())
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .categoryName("Shirt")
                .variations(List.of(variation))
                .build();

        VariationRequest variationRequest = VariationRequest.builder()
                .categoryId(categoryId)
                .name("Size")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.ofNullable(category));
        when(variationRepository.save(any(Variation.class))).thenReturn(variation);

        Variation variationSaved = variationService.createVariation(variationRequest);

        assertNotNull(variationSaved);
        assertEquals(variationRequest.getCategoryId(), variationSaved.getCategory().getId());
        assertEquals(variationRequest.getName(), variationSaved.getName());

        verify(categoryRepository).findById(categoryId);
        verify(variationRepository).save(any(Variation.class));
    }

    @Test
    void VariationService_CreateVariation_CategoryIsNull() {
        Integer categoryId = null;

        VariationRequest variationRequest = VariationRequest.builder()
                .categoryId(categoryId)
                .name("Size")
                .build();

        assertThrows(NotFoundException.class, () -> variationService.createVariation(variationRequest));

        verify(categoryRepository, never()).findById(any());
        verify(variationRepository, never()).save(any(Variation.class));
    }

    @Test
    void VariationService_GetAllVariation_Success() {
        Integer categoryId = 1;

        Category category = Category.builder()
                .id(categoryId)
                .categoryName("Shirt")
                .variations(List.of())
                .build();

        Variation variation1 = Variation.builder()
                .id(1)
                .name("Size")
                .category(category)
                .variationOptions(new ArrayList<>())
                .build();

        Variation variation2 = Variation.builder()
                .id(2)
                .name("Colour")
                .category(category)
                .variationOptions(new ArrayList<>())
                .build();

        List<Variation> variations = List.of(variation1, variation2);

        when(variationRepository.findAll()).thenReturn(variations);

        List<VariationResponseCatName> result = variationService.getAllVariation();

        assertNotNull(result);
        assertEquals(2, result.size());

        VariationResponseCatName response1 = result.get(0);
        assertEquals(variation1.getId(), response1.getId());
        assertEquals(variation1.getName(), response1.getName());
        assertEquals(variation1.getCategory().getCategoryName(), response1.getCategoryName());

        VariationResponseCatName response2 = result.get(1);
        assertEquals(variation2.getId(), response2.getId());
        assertEquals(variation2.getName(), response2.getName());
        assertEquals(variation2.getCategory().getCategoryName(), response2.getCategoryName());

        verify(variationRepository).findAll();
    }

    @Test
    void VariationService_GetVariationById_Success() {
        Integer variationId = 1;

        Category category = Category.builder()
                .id(1)
                .categoryName("Shirt")
                .variations(List.of())
                .build();

        Variation variation = Variation.builder()
                .id(variationId)
                .name("Size")
                .category(category)
                .variationOptions(new ArrayList<>())
                .build();

        when(variationRepository.findById(variationId)).thenReturn(Optional.ofNullable(variation));

        Variation result = variationService.getVariationById(variationId);

        assertNotNull(result);
        assertEquals(variation.getId(), result.getId());
        assertEquals(variation.getName(), result.getName());
        assertEquals(variation.getCategory().getCategoryName(), result.getCategory().getCategoryName());

        verify(variationRepository).findById(variationId);
    }

    @Test
    void VariationService_GetVariationByCategoryId_Success() {
        Integer categoryId = 1;

        Category category = Category.builder()
                .id(categoryId)
                .categoryName("Shirt")
                .variations(List.of())
                .build();

        Variation variation1 = Variation.builder()
                .id(1)
                .name("Size")
                .category(category)
                .variationOptions(new ArrayList<>())
                .build();

        Variation variation2 = Variation.builder()
                .id(2)
                .name("Colour")
                .category(category)
                .variationOptions(new ArrayList<>())
                .build();

        List<Variation> variations = List.of(variation1, variation2);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.ofNullable(category));
        when(variationRepository.findByCategoryId(categoryId)).thenReturn(variations);

        List<Variation> result = variationService.getVariationByCategoryId(categoryId);

        assertNotNull(result);
        assertEquals(2, result.size());

        Variation response1 = result.get(0);
        assertEquals(variation1.getId(), response1.getId());
        assertEquals(variation1.getName(), response1.getName());
        assertEquals(variation1.getCategory().getCategoryName(), response1.getCategory().getCategoryName());

        Variation response2 = result.get(1);
        assertEquals(variation2.getId(), response2.getId());
        assertEquals(variation2.getName(), response2.getName());
        assertEquals(variation2.getCategory().getCategoryName(), response2.getCategory().getCategoryName());

        verify(variationRepository).findByCategoryId(categoryId);
    }

    @Test
    void VariationService_GetVariationByCategoryId_CategoryNotFound() {
        Integer categoryId = 1;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> variationService.getVariationByCategoryId(categoryId));

        verify(categoryRepository).findById(categoryId);
        verify(variationRepository, never()).findByCategoryId(anyInt());
    }

    @Test
    void VariationService_UpdateVariation_Success() {
        Integer categoryId = 1;
        Integer variationId = 1;

        Variation variation = Variation.builder()
                .id(1)
                .name("Shirt")
                .category(Category.builder().id(categoryId).build())
                .variationOptions(new ArrayList<>())
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .categoryName("Shirt")
                .variations(List.of(variation))
                .build();

        VariationRequest updateVariationRequest = VariationRequest.builder()
                .categoryId(categoryId)
                .name("Size")
                .build();

        when(variationRepository.findById(variationId)).thenReturn(Optional.of(variation));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.ofNullable(category));
        when(variationRepository.save(any(Variation.class))).thenReturn(variation);

        Variation variationSaved = variationService.updateVariation(variationId, updateVariationRequest);

        assertNotNull(variationSaved);
        assertEquals("Size", variationSaved.getName());
        assertEquals(categoryId, variationSaved.getCategory().getId());
        verify(variationRepository).findById(variationId);
        verify(variationRepository).save(any(Variation.class));
    }

    @Test
    void VariationService_DeleteVariation_Success() {
        Integer categoryId = 1;
        Integer variationId = 1;

        List<Variation> variations = new ArrayList<>();

        Category category = Category.builder()
                .id(categoryId)
                .categoryName("Shirt")
                .variations(variations)
                .build();

        Variation variation = Variation.builder()
                .id(variationId)
                .name("Shirt")
                .category(category)
                .variationOptions(new ArrayList<>())
                .build();

        category.getVariations().add(variation);

        when(variationRepository.findById(variationId)).thenReturn(Optional.of(variation));
        when(categoryRepository.save(category)).thenReturn(category);

        variationService.deleteVariation(variationId);

        verify(variationRepository).delete(variation);

        verify(categoryRepository).save(category);
    }
}
