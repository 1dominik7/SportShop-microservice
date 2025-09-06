package com.ecommerce.user.discountCode;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("discount")
public class DiscountCodeController {

    private final DiscountCodeService discountCodeService;

    @PostMapping
    public ResponseEntity<DiscountCodeResponse> createDiscountCode(@RequestBody DiscountCodeRequest discountCodeRequest){
        DiscountCodeResponse discountCode = discountCodeService.createDiscountCode(discountCodeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(discountCode);
    }

    @PutMapping("{discountCodeId}")
    public ResponseEntity<DiscountCode> updateDiscount(@PathVariable String discountCodeId, @RequestBody DiscountCodeRequest discountCodeRequest) {
        DiscountCode discountCode = discountCodeService.updateDiscountCode(discountCodeId, discountCodeRequest);
        return ResponseEntity.ok(discountCode);
    }

    @GetMapping("{discountCodeId}")
    public ResponseEntity<DiscountCode> getDiscountCodeById(@PathVariable String discountCodeId) {
        DiscountCode discountCode = discountCodeService.getDiscountCodeById(discountCodeId);
        return ResponseEntity.ok(discountCode);
    }

    @GetMapping
    public ResponseEntity<List<DiscountCode>> getAllDiscountCodes() {
        List<DiscountCode> discountCode = discountCodeService.getAllDiscountCode();
        return ResponseEntity.ok(discountCode);
    }

    @DeleteMapping("{discountCodeId}")
    public ResponseEntity<String> deleteDiscountCode(@PathVariable String discountCodeId){
        discountCodeService.deleteDiscountCode(discountCodeId);
        return ResponseEntity.ok("Discount code has been successfully deleted!");
    }

    @GetMapping("/active")
    public ResponseEntity<List<DiscountCode>> getActiveDiscountCode() {
        List<DiscountCode> discountCodes = discountCodeService.getActiveDiscountCode();
        return ResponseEntity.ok(discountCodes);
    }
}
