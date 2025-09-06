package com.ecommerce.user.address;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("address")
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/create")
    public ResponseEntity<AddressResponse> createAddress(@RequestBody AddressRequest addressRequest, @AuthenticationPrincipal Jwt jwt){
        String currentKeycloakId = jwt.getSubject();
        AddressResponse address = addressService.createAddress(addressRequest,currentKeycloakId);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(@AuthenticationPrincipal Jwt jwt){
        String currentKeycloakId = jwt.getSubject();
        List<AddressResponse> addressResponseList = addressService.getUserAddresses(currentKeycloakId);
        return ResponseEntity.ok(addressResponseList);
    }

    @GetMapping("byId/{addressId}")
    public ResponseEntity<AddressResponse> getUserAddresses(@PathVariable String addressId, @AuthenticationPrincipal Jwt jwt){
        String currentKeycloakId = jwt.getSubject();
        AddressResponse addressResponseList = addressService.getAddressById(addressId,currentKeycloakId );
        return ResponseEntity.ok(addressResponseList);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable String addressId, @RequestBody AddressRequest addressRequest, @AuthenticationPrincipal Jwt jwt){
        String currentKeycloakId = jwt.getSubject();
        AddressResponse address = addressService.updateAddress(addressId, addressRequest, currentKeycloakId);
        return ResponseEntity.status(HttpStatus.OK).body(address);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable String addressId, @AuthenticationPrincipal Jwt jwt){
        String currentKeycloakId = jwt.getSubject();
        addressService.deleteAddress(addressId,currentKeycloakId);
        return ResponseEntity.ok("Address has been successfully deleted!");
    }
}
