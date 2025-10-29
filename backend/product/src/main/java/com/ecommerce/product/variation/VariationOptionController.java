package com.ecommerce.product.variation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("variation-option")
public class VariationOptionController {

    private final VariationOptionService variationOptionService;

    @PostMapping
    public ResponseEntity<VariationOption> createVariationOption(@RequestBody VariationOptionRequest variationOptionRequest){
        VariationOption variationOption = variationOptionService.createVariationOption(variationOptionRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(variationOption);
    }

    @GetMapping
    public ResponseEntity<List<VariationOptionWithVariationResponse>> getAllVariationOptions(){
        List<VariationOptionWithVariationResponse> variationOptions = variationOptionService.getAllVariationOptions();
        return ResponseEntity.ok(variationOptions);
    }

    @GetMapping("{variationOptionId}")
    public ResponseEntity<VariationOption> getVariationOptionById(@PathVariable Integer variationOptionId){
        VariationOption variationOption = variationOptionService.getVariationOptionById(variationOptionId);
        return ResponseEntity.ok(variationOption);
    }

    @PutMapping("/{variationOptionId}")
    public ResponseEntity<VariationOption> updateVariationOption(@PathVariable Integer variationOptionId, @RequestBody VariationOptionRequest variationOptionRequest){
        VariationOption variationOption = variationOptionService.updateVariationOption(variationOptionId, variationOptionRequest);
        return ResponseEntity.ok(variationOption);
    }

    @DeleteMapping("/{variationOptionId}")
    public ResponseEntity<String> deleteVariationOption(@PathVariable Integer variationOptionId){
        variationOptionService.deleteVariationOption(variationOptionId);
       return ResponseEntity.ok("Variation Option has been successfully deleted!");
    }
}
