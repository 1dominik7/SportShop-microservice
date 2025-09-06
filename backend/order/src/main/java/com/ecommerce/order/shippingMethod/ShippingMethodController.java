package com.ecommerce.order.shippingMethod;

import com.cloudinary.api.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("shipping-method")
public class ShippingMethodController {

    private final ShippingMethodService shippingMethodService;

    @PostMapping
    public ResponseEntity<ShippingMethod> addShippingMethod(@RequestBody ShippingMethodRequest shippingMethodRequest) throws ApiException {

        ShippingMethod shippingMethod = shippingMethodService.addShippingMethod(shippingMethodRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(shippingMethod);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ShippingMethodResponse>> getAllShippingMethod(){
        List<ShippingMethodResponse> shippingMethods = shippingMethodService.getAllShippingMethod();
        return ResponseEntity.status(HttpStatus.OK).body(shippingMethods);
    }

    @PutMapping("/{shippingMethodId}")
    public ResponseEntity<ShippingMethodResponse> updateShippingMethod(@RequestBody ShippingMethodRequest shippingMethodRequest,@PathVariable Integer shippingMethodId){
        ShippingMethodResponse shippingMethod = shippingMethodService.updateShippingMethod(shippingMethodRequest,shippingMethodId);
        return ResponseEntity.status(HttpStatus.OK).body(shippingMethod);
    }

    @DeleteMapping("/{shippingMethodId}")
    public ResponseEntity<String> deleteShippingMethod(@PathVariable Integer shippingMethodId){
        shippingMethodService.deleteShippingMethod(shippingMethodId);
        return ResponseEntity.ok("Shipping method has been successfully deleted!");
    }
}
