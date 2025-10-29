package com.ecommerce.product.variation;

import com.ecommerce.product.exceptions.NotFoundException;
import com.ecommerce.product.product.productItem.ProductItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariationOptionService {

    private final VariationOptionRepository variationOptionRepository;
    private final VariationRepository variationRepository;

    @Transactional
    public VariationOption createVariationOption(VariationOptionRequest variationOptionRequest) {
        Variation variation = null;
        if (variationOptionRequest.getVariationId() != null) {
            variation = variationRepository.findById(variationOptionRequest.getVariationId()).orElseThrow(() ->
                    new NotFoundException("Variation", Optional.of(variationOptionRequest.getVariationId().toString())));
        }

        VariationOption variationOption = VariationOption.builder()
                .value(variationOptionRequest.getValue())
                .variation(variation)
                .build();

        return variationOptionRepository.save(variationOption);
    }

    public List<VariationOptionWithVariationResponse> getAllVariationOptions() {
        List<VariationOption> variationOptions = variationOptionRepository.findAll();
        return variationOptions.stream()
                .map(variationOption -> new VariationOptionWithVariationResponse(
                        variationOption.getId(),
                        variationOption.getValue(),
                        new VariationShortResponse(
                                variationOption.getVariation().getId(),
                                variationOption.getVariation().getName(),
                                variationOption.getVariation().getCategory().getCategoryName()
                        )
                )).collect(Collectors.toList());
    }

    public VariationOption getVariationOptionById(Integer variationOptionId){
        return variationOptionRepository.findById(variationOptionId).orElseThrow(() ->
                new NotFoundException("Variation option", Optional.of(variationOptionId.toString())));
    }

    @Transactional
    public VariationOption updateVariationOption(Integer variationOptionId, VariationOptionRequest variationOptionRequest) {
        VariationOption variationOption = variationOptionRepository.findById(variationOptionId).orElseThrow(() ->
                new NotFoundException("Variation option", Optional.of(variationOptionId.toString())));

        if (variationOptionRequest.getValue() != null) {
            variationOption.setValue(variationOptionRequest.getValue());
        }

        if (variationOptionRequest.getVariationId() != null) {
            Variation variation = variationRepository.findById(variationOptionRequest.getVariationId()).orElseThrow(() ->
                    new NotFoundException("Variation", Optional.of(variationOptionRequest.getVariationId().toString())));
            variationOption.setVariation(variation);
        }

        return variationOptionRepository.save(variationOption);
    }

    @Transactional
    public void deleteVariationOption(Integer variationOptionId) {
        VariationOption variationOption = variationOptionRepository.findById(variationOptionId).orElseThrow(() ->
                new NotFoundException("Variation option", Optional.of(variationOptionId.toString())));

        if(variationOption.getVariation() != null){
            variationOption.getVariation().getVariationOptions().remove(variationOption);
        }

        if(variationOption.getProductItems() != null){
            for (ProductItem productItem : variationOption.getProductItems()){
                productItem.getVariationOptions().remove(variationOption);
            }
        }

        variationOptionRepository.delete(variationOption);
    }

}
