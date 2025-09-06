package com.ecommerce.order.shippingMethod;

import com.cloudinary.api.exceptions.ApiException;
import com.ecommerce.order.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingMethodService {

    private final ShippingMethodRepository shippingMethodRepository;

    @Transactional
    public ShippingMethod addShippingMethod(ShippingMethodRequest shippingMethodRequest) throws ApiException {

        Optional<ShippingMethod> shippingMethodExisting = shippingMethodRepository.findByName(shippingMethodRequest.getName());

        if(shippingMethodExisting.isPresent()){
            throw new ApiException("Shipping method with " + shippingMethodRequest.getName() +" already exists");
        }

        ShippingMethod shippingMethod = new ShippingMethod();
        shippingMethod.setName(shippingMethodRequest.name);
        shippingMethod.setPrice(shippingMethodRequest.price);

        return shippingMethodRepository.save(shippingMethod);
    }

    public List<ShippingMethodResponse> getAllShippingMethod() {
        return shippingMethodRepository.findAll().stream().map(shippingMethod -> ShippingMethodResponse.builder()
                .id(shippingMethod.getId())
                .name(shippingMethod.getName())
                .price(shippingMethod.getPrice()
                ).build()).collect(Collectors.toList());
    }

    @Transactional
    public ShippingMethodResponse updateShippingMethod(ShippingMethodRequest shippingMethodRequest,Integer shippingMethodId){
        ShippingMethod shippingMethod = shippingMethodRepository.findById(shippingMethodId).orElseThrow(() -> new NotFoundException("Shipping method", Optional.of(shippingMethodId.toString())));

        shippingMethod.setName(shippingMethodRequest.getName());
        shippingMethod.setPrice(shippingMethodRequest.getPrice());

        ShippingMethod updated = shippingMethodRepository.save(shippingMethod);

        return ShippingMethodResponse.builder()
                .id(updated.getId())
                .name(updated.getName())
                .price(updated.getPrice())
                .build();
    }

    @Transactional
    public void deleteShippingMethod(Integer shippingMethodId){
        ShippingMethod shippingMethod = shippingMethodRepository.findById(shippingMethodId).orElseThrow(() -> new NotFoundException("Shipping method", Optional.of(shippingMethodId.toString())));
        shippingMethodRepository.deleteById(shippingMethod.getId());
    }
}
