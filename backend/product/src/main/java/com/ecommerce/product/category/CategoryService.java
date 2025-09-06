package com.ecommerce.product.category;

import com.ecommerce.product.exceptions.NotFoundException;
import com.ecommerce.product.variation.Variation;
import com.ecommerce.product.variation.VariationRepository;
import com.ecommerce.product.variation.VariationResponse;
import com.ecommerce.product.variation.VariationOptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final VariationRepository variationRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {

        if (categoryRequest.getCategoryName() == null || categoryRequest.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or blank");
        }

        Category category = new Category();
        category.setCategoryName(categoryRequest.getCategoryName());

        if (categoryRequest.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryRequest.getParentCategoryId()).orElseThrow(() ->
                    new NotFoundException("Parent category", Optional.of(categoryRequest.getParentCategoryId().toString())));
            category.setParentCategory(parentCategory);
        }

        Category savedCategory = categoryRepository.save(category);

        List<Integer> variationIds = savedCategory.getVariations().stream().map(
                variation -> variation.getId()
        ).collect(Collectors.toList());

        CategoryResponse categoryResponse = new CategoryResponse(
                savedCategory.getId(),
                savedCategory.getCategoryName(),
                savedCategory.getParentCategory() != null ? savedCategory.getParentCategory().getId() : null,
                variationIds
        );

        return categoryResponse;
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(category -> {
            List<Variation> variations = variationRepository.findByCategoryId(category.getId());

            List<Integer> variationIds = variations.stream().map(Variation::getId)
                    .collect(Collectors.toList());

            CategoryResponse categoryResponse = modelMapper.map(category, CategoryResponse.class);

            categoryResponse.setVariationIds(variationIds);
            return categoryResponse;
        }).collect(Collectors.toList());
    }

    public List<CategoryWithVariationResponse> getCategoryById(List<Integer> categoryIds) {

        List<Category> categories = categoryRepository.findAllById(categoryIds);

        if (categories.isEmpty()) {
            String idsAsString = categoryIds.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
            throw new NotFoundException("Categories", Optional.of(idsAsString));
        }

        return categories.stream()
                .map(category -> {
                    CategoryWithVariationResponse categoryResponse = modelMapper.map(category, CategoryWithVariationResponse.class);

                    List<VariationResponse> variationResponses = categoryResponse.getVariations().stream()
                            .map(variation -> {
                                List<VariationOptionResponse> options = variation.getOptions().stream()
                                        .map(option -> new VariationOptionResponse(option.getId(), option.getValue()))
                                        .collect(Collectors.toList());

                                return new VariationResponse(variation.getId(), variation.getCategoryId(), variation.getName(), options);
                            })
                            .collect(Collectors.toList());

                    categoryResponse.setVariations(variationResponses);
                    return categoryResponse;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Category updateCategory(Integer categoryId, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category", Optional.of(categoryId.toString())));

        category.setCategoryName(categoryRequest.getCategoryName());

        if (categoryRequest.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryRequest.getParentCategoryId())
                    .orElseThrow(() -> new NotFoundException("Parent", Optional.of(categoryRequest.getParentCategoryId().toString())));
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null);
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Category", Optional.of(categoryId.toString())));

        Category parentCategory = category.getParentCategory();
        if (parentCategory != null) {
            parentCategory.getSubcategories().remove(category);
            categoryRepository.save(parentCategory);
        }

        for (Category subcategory : category.getSubcategories()) {
            subcategory.setParentCategory(null);
            categoryRepository.save(subcategory);
        }
        categoryRepository.delete(category);
    }

}