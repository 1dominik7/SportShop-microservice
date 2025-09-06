package com.ecommerce.product.category;

import com.ecommerce.product.exceptions.NotFoundException;
import com.ecommerce.product.variation.Variation;
import com.ecommerce.product.variation.VariationOptionRepository;
import com.ecommerce.product.variation.VariationRepository;
import com.ecommerce.product.variation.VariationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private VariationOptionRepository variationOptionRepository;

    @Mock
    private VariationRepository variationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private Jwt jwt;

    @Test
    void CategoryService_CreateCategory_Success() {
        Integer categoryId = 1;

        CategoryRequest categoryRequest = CategoryRequest.builder()
                .categoryName("Shirt")
                .parentCategoryId(null)
                .build();

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

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse categorySaved = categoryService.createCategory(categoryRequest);

        assertNotNull(categorySaved);
        assertEquals(categoryRequest.getCategoryName(), categorySaved.getCategoryName());
        assertEquals(categoryRequest.getParentCategoryId(), categorySaved.getParentCategoryId());

        verify(categoryRepository).save(any(Category.class));
        verify(categoryRepository, times(0)).findById(anyInt());
    }

    @Test
    void CategoryService_CreateCategory_ThrowsIllegalArgumentException() {

        CategoryRequest categoryRequestWithNullName = CategoryRequest.builder()
                .categoryName(null)
                .parentCategoryId(null)
                .build();

        CategoryRequest categoryRequestWithBlankName = CategoryRequest.builder()
                .categoryName(null)
                .parentCategoryId(null)
                .build();

        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(categoryRequestWithNullName));
        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(categoryRequestWithBlankName));
    }

    @Test
    void CategoryService_CreateCategory_ThrowsNotFoundException() {
        Integer parentCategoryId = 10;

        CategoryRequest categoryRequest = CategoryRequest.builder()
                .categoryName("Shirt")
                .parentCategoryId(parentCategoryId)
                .build();

        when(categoryRepository.findById(eq(parentCategoryId))).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> categoryService.createCategory(categoryRequest));
    }

    @Test
    void CategoryService_GetAllCategories_Success() {

        Variation variation1 = Variation.builder()
                .id(1)
                .name("Size")
                .category(Category.builder().id(1).build())
                .variationOptions(new ArrayList<>())
                .build();

        Variation variation2 = Variation.builder()
                .id(2)
                .name("Size")
                .category(Category.builder().id(2).build())
                .variationOptions(new ArrayList<>())
                .build();


        Category category1 = Category.builder()
                .id(1)
                .categoryName("Shirt")
                .build();

        Category category2 = Category.builder()
                .id(2)
                .categoryName("Boots")
                .build();

        List<Category> categories = List.of(category1, category2);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(variationRepository.findByCategoryId(1)).thenReturn(List.of(variation1));
        when(variationRepository.findByCategoryId(2)).thenReturn(List.of(variation2));

        CategoryResponse categoryResponse1 = new CategoryResponse(1, "Shirt", null, List.of(1));
        CategoryResponse categoryResponse2 = new CategoryResponse(2, "Pants", null, List.of(2));

        when(modelMapper.map(category1, CategoryResponse.class)).thenReturn(categoryResponse1);
        when(modelMapper.map(category2, CategoryResponse.class)).thenReturn(categoryResponse2);

        List<CategoryResponse> categoryResponses = categoryService.getAllCategories();

        assertNotNull(categoryResponses);
        assertEquals(2, categoryResponses.size());
        assertEquals("Shirt", categoryResponses.get(0).getCategoryName());
        assertEquals(List.of(1), categoryResponses.get(0).getVariationIds());
        assertEquals("Pants", categoryResponses.get(1).getCategoryName());
        assertEquals(List.of(2), categoryResponses.get(1).getVariationIds());

        verify(categoryRepository).findAll();
        verify(variationRepository).findByCategoryId(1);
        verify(variationRepository).findByCategoryId(2);
    }

    @Test
    void CategoryService_UpdateCategory_Success() {
        Integer categoryId = 1;

        CategoryRequest categoryRequest = CategoryRequest.builder()
                .categoryName("Colour")
                .parentCategoryId(2)
                .build();

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

        Category parentCategory = Category.builder()
                .id(2)
                .categoryName("Parent Category")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category updatedCategory = categoryService.updateCategory(categoryId, categoryRequest);

        assertNotNull(updatedCategory);
        assertEquals(categoryRequest.getCategoryName(), updatedCategory.getCategoryName());
        assertNotNull(updatedCategory.getParentCategory());
        assertEquals(2, updatedCategory.getParentCategory().getId());
        assertEquals("Parent Category", updatedCategory.getParentCategory().getCategoryName());

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).findById(2);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void CategoryService_DeleteCategory_Success() {
        Integer categoryId = 1;

        Category category = Category.builder()
                .id(categoryId)
                .categoryName("Shirt")
                .variations(List.of())
                .subcategories(List.of())
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository).delete(category);
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void CategoryService_DeleteCategory_CategoryNotFound() {
        Integer categoryId = 1;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.deleteCategory(categoryId));

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, times(0)).delete(any());
    }
}
