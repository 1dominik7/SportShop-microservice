package com.ecommerce.product.variation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("variation")
public class VariationController {

    private final VariationService variationService;

    @PostMapping
    public ResponseEntity<Variation> createVariation(@RequestBody VariationRequest variationRequest) {
        Variation variation = variationService.createVariation(variationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(variation);
    }

    @GetMapping
    public ResponseEntity<List<VariationResponseCatName>> getAllVariation() {
        List<VariationResponseCatName> variations = variationService.getAllVariation();
        return ResponseEntity.ok(variations);
    }

    @GetMapping("/{variationId}")
    public ResponseEntity<Variation> getVariationById(@PathVariable Integer variationId) {
        Variation variation = variationService.getVariationById(variationId);
        return ResponseEntity.ok(variation);
    }

    @GetMapping("byCategory/{categoryId}")
    public ResponseEntity<List<Variation>> getVariationByCategoryId(@PathVariable Integer categoryId){
        List<Variation> variations = variationService.getVariationByCategoryId(categoryId);
        return ResponseEntity.ok(variations);
    }

    @PutMapping("/{variationId}")
    public ResponseEntity<Variation> updatedVariation(@PathVariable Integer variationId, @RequestBody VariationRequest variation) {
        Variation updatedVariation = variationService.updateVariation(variationId, variation);
        return ResponseEntity.ok(updatedVariation);
    }

    @DeleteMapping("/{variationId}")
    public ResponseEntity<String> deleteVariation(@PathVariable Integer variationId) {
        variationService.deleteVariation(variationId);
        return ResponseEntity.ok("Variation has been successfully deleted!");
    }
}
