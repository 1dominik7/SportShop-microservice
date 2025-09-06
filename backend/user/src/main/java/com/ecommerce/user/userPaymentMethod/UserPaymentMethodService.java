package com.ecommerce.user.userPaymentMethod;

import com.ecommerce.user.clients.PaymentClient;
import com.ecommerce.user.clients.dto.PaymentTypeResponse;
import com.ecommerce.user.exceptions.NotFoundException;
import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPaymentMethodService {

    private final UserRepository userRepository;
    private final PaymentClient paymentClient;

    @Transactional
    public UserPaymentMethod createUserPaymentMethod(UserPaymentMethodRequest request, String currentKeycloakId) {
        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        if (request.getPaymentTypeId() == null) {
            throw new IllegalArgumentException("Payment Type ID is required");
        }

        PaymentTypeResponse paymentType = paymentClient.paymentTypeResponse().stream().filter(p -> p.getId().equals(request.getPaymentTypeId())).findFirst()
                .orElseThrow(() -> new NotFoundException("Payment type" , Optional.of(request.getPaymentTypeId().toString())));

        UserPaymentMethod userPaymentMethod = UserPaymentMethod.builder()
                .id(UUID.randomUUID().toString())
                .paymentType(paymentType)
                .provider(request.getProvider())
                .last4CardNumber(request.getLast4CardNumber())
                .paymentDate(request.getPaymentDate())
                .expiryDate(request.getExpiryDate())
                .isDefault(request.isDefault())
                .active(true)
                .build();

        if (request.isDefault()) {
            setAsDefaultPaymentMethod(user, userPaymentMethod);
        }

        if (user.getUserPaymentMethods() == null) {
            user.setUserPaymentMethods(new ArrayList<>());
        }
        user.getUserPaymentMethods().add(userPaymentMethod);
        userRepository.save(user);
        return userPaymentMethod;
    }

    private void setAsDefaultPaymentMethod(User user, UserPaymentMethod newDefaultMethod) {
        List<UserPaymentMethod> existingMethods = user.getUserPaymentMethods();

        existingMethods.forEach(method -> method.setDefault(false));

        newDefaultMethod.setDefault(true);
    }

    public List<UserPaymentMethodResponse> getUserPaymentMethods (String currentKeycloakId){

        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));


        return user.getUserPaymentMethods().stream()
                .map(pm -> buildResponse(pm))
                .collect(Collectors.toList());
    }

    public UserPaymentMethodResponse getUserPaymentMethodById(String paymentMethodId, String currentKeycloakId){

        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        UserPaymentMethod paymentMethod = user.getUserPaymentMethods().stream()
                .filter(pm -> pm.getId().equals(paymentMethodId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Payment method", Optional.of(paymentMethodId)));

        return buildResponse(paymentMethod);
    }

    @Transactional
    public void deleteUserPaymentMethodById(String paymentMethodId, String currentKeycloakId){
        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        UserPaymentMethod paymentMethod = user.getUserPaymentMethods().stream()
                .filter(pm -> pm.getId().equals(paymentMethodId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Payment method", Optional.of(paymentMethodId)));

        paymentMethod.setActive(false);
        userRepository.save(user);
    }

    private UserPaymentMethodResponse buildResponse(UserPaymentMethod userPaymentMethod){
        return UserPaymentMethodResponse.builder()
                .id(userPaymentMethod.getId())
                .paymentTypeName(userPaymentMethod.getPaymentType().getValue())
                .provider(userPaymentMethod.getProvider())
                .last4CardNumber(userPaymentMethod.getLast4CardNumber())
                .paymentDate(userPaymentMethod.getPaymentDate())
                .expiryDate(userPaymentMethod.getExpiryDate())
                .isDefault(userPaymentMethod.isDefault())
                .active(userPaymentMethod.isActive())
                .build();
    }
}
