package com.ecommerce.product.variation;

import com.ecommerce.product.category.Category;
import com.ecommerce.product.category.CategoryRepository;
import com.ecommerce.product.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariationService {

    private final VariationRepository variationRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Variation createVariation(VariationRequest variationRequest) {
        if (variationRequest.getCategoryId() == null) {
            throw new NotFoundException("Category", Optional.empty());
        }

        Category category = categoryRepository.findById(variationRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category", Optional.of(variationRequest.getCategoryId().toString())));


        Variation variation = Variation.builder()
                .name(variationRequest.getName())
                .category(category)
                .variationOptions(new ArrayList<>())
                .build();

        return variationRepository.save(variation);
    }

    public List<VariationResponseCatName> getAllVariation() {
        return variationRepository.findAll().stream()
                .map(v -> new VariationResponseCatName(
                        v.getId(),
                        v.getName(),
                        v.getCategory().getCategoryName(),
                        v.getCategory().getId(),
                        v.getVariationOptions()
                )).collect(Collectors.toList());
    }

    public Variation getVariationById(Integer variationId) {
        return variationRepository.findById(variationId).orElseThrow(() ->
                new NotFoundException("Variation", Optional.of(variationId.toString())));
    }

    public List<Variation> getVariationByCategoryId(Integer categoryId) {
        if (categoryId == null) {
            throw new NotFoundException("Variation", Optional.empty());
        }
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category", Optional.of(categoryId.toString())));

        return variationRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Variation updateVariation(Integer variationId, VariationRequest variationRequest) {
        Category category = null;
        Variation variation = variationRepository.findById(variationId).orElseThrow(() ->
                new NotFoundException("Variation", Optional.of(variationId.toString())));

        if (variationRequest.getCategoryId() != null) {
            category = categoryRepository.findById(variationRequest.getCategoryId()).orElseThrow(() ->
                    new NotFoundException("Category", Optional.of(variationRequest.getCategoryId().toString())));
            variation.setCategory(category);
        } else {
            variation.setCategory(null);
        }

        if (!variationRequest.getName().isEmpty()) {
            variation.setName(variationRequest.getName());
        }

        return variationRepository.save(variation);
    }

    @Transactional
    public void deleteVariation(Integer variationId) {
        Variation variation = variationRepository.findById(variationId).orElseThrow(() ->
                new NotFoundException("Variation", Optional.of(variationId.toString())));

        Category category = variation.getCategory();
        if (category != null) {
            category.getVariations().remove(variation);

            categoryRepository.save(category);
        }

        variationRepository.delete(variation);
    }
}
