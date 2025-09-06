package com.ecommerce.order.shippingMethod;

import com.cloudinary.api.exceptions.ApiException;
import com.ecommerce.order.exceptions.NotFoundException;
import com.ecommerce.order.orderStatus.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShippingMethodServiceTest {

    @Mock
    private ShippingMethodRepository shippingMethodRepository;

    @InjectMocks
    private ShippingMethodService shippingMethodService;

    @Test
    void ShippingMethodService_CreateShippingMethod_Success() throws ApiException {

        ShippingMethodRequest shippingMethodRequest = ShippingMethodRequest.builder()
                .name("Test delivery")
                .price(10.0)
                .build();

        ShippingMethod shippingMethod = ShippingMethod.builder()
                .id(1)
                .name("Test delivery")
                .price(10.0)
                .build();

        when(shippingMethodRepository.findByName(shippingMethodRequest.getName())).thenReturn(Optional.empty());
        when(shippingMethodRepository.save(any(ShippingMethod.class))).thenReturn(shippingMethod);

        ShippingMethod response = shippingMethodService.addShippingMethod(shippingMethodRequest);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Test delivery", response.getName());

        verify(shippingMethodRepository).findByName("Test delivery");
        verify(shippingMethodRepository).save(any(ShippingMethod.class));
    }

    @Test
    void ShippingMethodService_CreateShippingMethod_NameAlreadyExists() {

        ShippingMethodRequest shippingMethodRequest = ShippingMethodRequest.builder()
                .name("Test delivery")
                .price(10.0)
                .build();

        ShippingMethod shippingMethod = ShippingMethod.builder()
                .id(1)
                .name("Test delivery")
                .price(10.0)
                .build();

        when(shippingMethodRepository.findByName(shippingMethodRequest.getName())).thenReturn(Optional.ofNullable(shippingMethod));

        ApiException exception = assertThrows(ApiException.class, () -> {
            shippingMethodService.addShippingMethod(shippingMethodRequest);
        });

        assertEquals("Shipping method with " + shippingMethodRequest.getName() + " already exists", exception.getMessage());

        verify(shippingMethodRepository).findByName("Test delivery");
        verify(shippingMethodRepository, never()).save(any());
    }

    @Test
    void ShippingMethodService_GetAllShippingMethod_Success() throws ApiException {

        ShippingMethod shippingMethod1 = ShippingMethod.builder()
                .id(1)
                .name("Test delivery1")
                .price(10.0)
                .build();


        ShippingMethod shippingMethod2 = ShippingMethod.builder()
                .id(2)
                .name("Test delivery2")
                .price(11.0)
                .build();

        when(shippingMethodRepository.findAll()).thenReturn(List.of(shippingMethod1,shippingMethod2));

        List<ShippingMethodResponse> response = shippingMethodService.getAllShippingMethod();

        assertEquals(2, response.size());
        assertEquals("Test delivery1", response.get(0).getName());
        assertEquals(10.0, response.get(0).getPrice());
        assertEquals("Test delivery2", response.get(1).getName());
        assertEquals(11.0, response.get(1).getPrice());

        verify(shippingMethodRepository).findAll();
    }

    @Test
    void ShippingMethodService_UpdateShippingMethod_Success() {
        Integer shippingMethodId = 1;
        ShippingMethodRequest shippingMethodRequest = ShippingMethodRequest.builder()
                .name("Test updated")
                .price(12.0)
                .build();

        ShippingMethod existing = ShippingMethod.builder()
                .id(shippingMethodId)
                .name("Test delivery")
                .price(10.0)
                .build();

        ShippingMethod updated = ShippingMethod.builder()
                .id(shippingMethodId)
                .name("Test updated")
                .price(12.0)
                .build();

        when(shippingMethodRepository.findById(shippingMethodId)).thenReturn(Optional.of(existing));
        when(shippingMethodRepository.save(existing)).thenReturn(updated);

        ShippingMethodResponse response = shippingMethodService.updateShippingMethod(shippingMethodRequest, shippingMethodId);

        assertNotNull(response);
        assertEquals(shippingMethodId, response.getId());
        assertEquals("Test updated", response.getName());
        assertEquals(12.0, response.getPrice());

        verify(shippingMethodRepository).findById(shippingMethodId);
        verify(shippingMethodRepository).save(existing);
    }

    @Test
    void ShippingMethodService_UpdateShippingMethod_NotFound() {
        Integer shippingMethodId = 1;
        ShippingMethodRequest shippingMethodRequest = ShippingMethodRequest.builder()
                .name("Test updated")
                .price(12.0)
                .build();

        when(shippingMethodRepository.findById(shippingMethodId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            shippingMethodService.updateShippingMethod(shippingMethodRequest, shippingMethodId);
        });

        assertEquals("Shipping method not found with id: Optional[999]", exception.getMessage());

        verify(shippingMethodRepository).findById(shippingMethodId);
        verify(shippingMethodRepository, never()).save(any());
    }

    @Test
    void ShippingMethodService_DeleteShippingMethod_Success() {
        ShippingMethod shippingMethod = ShippingMethod.builder()
                .id(1)
                .name("Test updated")
                .build();

        when(shippingMethodRepository.findById(1)).thenReturn(Optional.of(shippingMethod));

        shippingMethodService.deleteShippingMethod(1);

        verify(shippingMethodRepository).findById(1);
        verify(shippingMethodRepository).deleteById(shippingMethod.getId());
    }

}
