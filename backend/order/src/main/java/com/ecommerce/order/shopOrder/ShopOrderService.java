package com.ecommerce.order.shopOrder;

import com.ecommerce.order.clients.ProductItemCallerService;
import com.ecommerce.order.clients.UserCallerService;
import com.ecommerce.order.clients.dto.*;
import com.ecommerce.order.exceptions.APIException;
import com.ecommerce.order.exceptions.NotFoundException;
import com.ecommerce.order.orderLine.OrderLine;
import com.ecommerce.order.orderLine.OrderLineResponseWithProductItem;
import com.ecommerce.order.orderStatus.OrderStatus;
import com.ecommerce.order.orderStatus.OrderStatusRepository;
import com.ecommerce.order.shippingMethod.ShippingMethod;
import com.ecommerce.order.shippingMethod.ShippingMethodRepository;
import com.ecommerce.order.shippingMethod.ShippingMethodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopOrderService {

    private final ShopOrderRepository shopOrderRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final UserCallerService userCallerService;
    private final ProductItemCallerService productItemCallerService;

    @Transactional
    public ShopOrderResponse createShopOrder(ShopOrderRequest request, Jwt jwt) {

        UserResponse user = getAuthenticatedUser(jwt);

        ShoppingCartResponse cart = user.getShoppingCart();

        if (cart == null || cart.getShoppingCartItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty!");
        }

        AddressRequest addressRequest = request.getAddressRequest();
        AddressResponse selectedAddress;

        if (addressRequest.getId() != null) {
            selectedAddress = user.getAddresses().stream()
                    .filter(addr -> addr.getId().equals(addressRequest.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Address", Optional.ofNullable(addressRequest.getId())));
        } else {
            selectedAddress = AddressResponse.builder()
                    .firstName(addressRequest.getFirstName())
                    .lastName(addressRequest.getLastName())
                    .street(addressRequest.getStreet())
                    .city(addressRequest.getCity())
                    .postalCode(addressRequest.getPostalCode())
                    .country(addressRequest.getCountry())
                    .phoneNumber(addressRequest.getPhoneNumber())
                    .build();
        }

        ShippingMethod shippingMethod = shippingMethodRepository.findById(request.getShippingMethodId())
                .orElseThrow(() -> new NotFoundException("Shipping method", Optional.of(request.getShippingMethodId().toString())));


        OrderStatus initialStatus = orderStatusRepository.findByStatus("awaiting payment")
                .orElseThrow(() -> new NotFoundException("Initial order status", Optional.of("awaiting payment")));

        ShopOrder order = ShopOrder.builder()
                .userId(user.getId())
                .orderDate(LocalDateTime.now())
                .shippingMethod(shippingMethod)
                .orderTotal(request.getOrderTotal())
                .finalOrderTotal(request.getFinalOrderTotal())
                .appliedDiscountValue(request.getAppliedDiscountValue())
                .orderStatus(initialStatus)
                .shippingFirstName(selectedAddress.getFirstName())
                .shippingLastName(selectedAddress.getLastName())
                .shippingStreet(selectedAddress.getStreet())
                .shippingCity(selectedAddress.getCity())
                .shippingPostalCode(selectedAddress.getPostalCode())
                .shippingCountry(selectedAddress.getCountry())
                .shippingPhoneNumber(selectedAddress.getPhoneNumber())
                .shippingAddressLine1(selectedAddress.getAddressLine1())
                .shippingAddressLine2(selectedAddress.getAddressLine2())
                .build();

        List<Integer> productItemIds = cart.getShoppingCartItems().stream()
                .map(item -> item.getProductItemId())
                .collect(Collectors.toList());
        List<ProductItemOneByColourResponse> products = productItemCallerService.getProductItemByIdsToCreateOrder(productItemIds);

        List<ProductItemOneByColour> allProductItems = products.stream()
                .flatMap(p -> p.getProductItemOneByColour().stream())
                .collect(Collectors.toList());

        Map<Integer, ProductItemOneByColour> productMap = allProductItems.stream()
                .collect(Collectors.toMap(
                        ProductItemOneByColour::getId,
                        p -> p,
                        (existing, replacement) -> existing
                ));

        List<OrderLine> orderLines = cart.getShoppingCartItems().stream()
                .map(item -> {
                    ProductItemOneByColour product = productMap.get(item.getProductItemId());
                    if (product == null) {
                        throw new NotFoundException("ProductItem", Optional.of(item.getProductItemId().toString()));
                    }

                    double basePrice = product.getPrice() != null ? product.getPrice() : 0.0;
                    Integer discount = product.getDiscount();

                    double finalPrice = (discount != null && discount > 0)
                            ? basePrice * (1 - ((double) discount / 100))
                            : basePrice;

                    return OrderLine.builder()
                            .productItemId(item.getProductItemId())
                            .productName(product.getProductName())
                            .qty(item.getQty())
                            .price(roundToTwoDecimalPlaces(finalPrice))
                            .shopOrder(order)
                            .userReviewIds(new ArrayList<>())
                            .build();
                })
                .collect(Collectors.toList());

        order.setOrderLines(orderLines);

        ShopOrder savedOrder = shopOrderRepository.save(order);
        return convertToShopOrderResponse(savedOrder);
    }

    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public List<ShopOrderResponse> getUserShopOrders(Jwt jwt) {
        UserResponse user = getAuthenticatedUser(jwt);

        List<ShopOrder> shopOrders = shopOrderRepository.findByUserId(user.getId());

        return shopOrders.stream()
                .map(this::convertToShopOrderResponse).collect(Collectors.toList());
    }

    public ShopOrderResponse getUserShopOrderById(Integer shopOrderId, Jwt jwt) {
        UserResponse user = getAuthenticatedUser(jwt);

        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId).orElseThrow(() -> new NotFoundException("Order", Optional.of(shopOrderId.toString())));

        if (!shopOrder.getUserId().equals(user.getId())) {
            throw new APIException("This order does not belong to the user");
        }

        return convertToShopOrderResponse(shopOrder);
    }

    public List<ShopOrderResponse> getUserShopOrdersWithProductItems(Jwt jwt) {
        UserResponse user = getAuthenticatedUser(jwt);

        List<ShopOrder> shopOrders = shopOrderRepository.findByUserId(user.getId());

        List<Integer> productItemIds = shopOrders.stream()
                .flatMap(order -> order.getOrderLines().stream())
                .map(OrderLine::getProductItemId)
                .collect(Collectors.toList());

        List<ProductItemToOrderResponse> productItems = productItemCallerService.getProductItemByIdsToOrders(productItemIds);

        Map<Integer, ProductItemToOrderResponse> productItemsMap = productItems.stream()
                .collect(Collectors.toMap(ProductItemToOrderResponse::getId, item -> item));

        List<ShopOrderResponse> orderResponses = shopOrders.stream()
                .map(order -> convertToShopOrderResponseWithProductItems(order, productItemsMap))
                .collect(Collectors.toList());

        return orderResponses;
    }

    public ShopOrderResponse convertToShopOrderResponseWithProductItems(ShopOrder order, Map<Integer, ProductItemToOrderResponse> productItemsMap) {
        if (order == null) {
            return null;
        }

        ShippingMethodResponse shippingMethodResponse = null;
        if (order.getShippingMethod() != null) {
            shippingMethodResponse = new ShippingMethodResponse(
                    order.getShippingMethod().getId(),
                    order.getShippingMethod().getName(),
                    order.getShippingMethod().getPrice()
            );
        }

        List<OrderLineResponseWithProductItem> orderLineResponses = order.getOrderLines().stream()
                .map(orderLine -> {
                    ProductItemToOrderResponse productItemResponse = productItemsMap.get(orderLine.getProductItemId());
                    return OrderLineResponseWithProductItem.builder()
                            .id(orderLine.getId())
                            .productName(orderLine.getProductName())
                            .productItem(productItemResponse)
                            .qty(orderLine.getQty())
                            .price(orderLine.getPrice())
                            .build();
                }).collect(Collectors.toList());

        return ShopOrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderDate(order.getOrderDate())
                .shippingMethod(shippingMethodResponse)
                .orderTotal(order.getOrderTotal())
                .finalOrderTotal(order.getFinalOrderTotal())
                .orderStatus(order.getOrderStatus())
                .orderLines(orderLineResponses)
                .paymentId(order.getPaymentId())
                .appliedDiscountValue(order.getAppliedDiscountValue())
                .paymentTransactionId(order.getPaymentTransactionId())
                .paymentIntentId(order.getPaymentIntentId())
                .paymentMethodName(order.getPaymentMethodName())
                .paymentCreatedAt(order.getPaymentCreatedAt())
                .paymentStatus(order.getPaymentStatus())
                .shippingFirstName(order.getShippingFirstName())
                .shippingLastName(order.getShippingLastName())
                .shippingStreet(order.getShippingStreet())
                .shippingCity(order.getShippingCity())
                .shippingPostalCode(order.getShippingPostalCode())
                .shippingCountry(order.getShippingCountry())
                .shippingPhoneNumber(order.getShippingPhoneNumber())
                .shippingAddressLine1(order.getShippingAddressLine1())
                .shippingAddressLine2(order.getShippingAddressLine2())
                .build();
    }

    private ProductItemToOrderResponse mapProductItemOneByColour(ProductItemOneByColour productItemOneByColour) {
        return ProductItemToOrderResponse.builder()
                .id(productItemOneByColour.getId())
                .price(productItemOneByColour.getPrice())
                .discount(productItemOneByColour.getDiscount())
                .productCode(productItemOneByColour.getProductCode())
                .qtyInStock(productItemOneByColour.getQtyInStock())
                .productId(productItemOneByColour.getProductId())
                .productName(productItemOneByColour.getProductName())
                .productDescription(productItemOneByColour.getProductDescription())
                .productImages(productItemOneByColour.getProductImages())
                .variationOptions(productItemOneByColour.getVariations().stream()
                        .flatMap(variation -> variation.getOptions().stream())
                        .map(option -> VariationOptionWithVariationResponse.builder()
                                .id(option.getId())
                                .value(option.getValue())
                                .variation(new VariationShortResponse(option.getId(), option.getValue()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public ShopOrderResponse getByPaymentIntentId(String paymentIntentId, Jwt jwt) {
        UserResponse user = getAuthenticatedUser(jwt);

        ShopOrder order = shopOrderRepository.findByPaymentIntentIdAndUserId(paymentIntentId, user.getId());

        if (order == null) {
            throw new NotFoundException("PaymentIntent", Optional.of(paymentIntentId));
        }

        if (!order.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order does not belong to the user");
        }

        return convertToShopOrderResponse(order);
    }

    @Transactional
    public ShopOrderResponse updateShopOrderById(Integer shopOrderId, ShopOrderPaymentUpdateRequest shopOrderUpdateRequest, Jwt jwt) {
        UserResponse user = getAuthenticatedUser(jwt);

        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId).orElseThrow(
                () -> new NotFoundException("Order", Optional.of(shopOrderId.toString())));

        if (!shopOrder.getUserId().equals(user.getId())) {
            throw new APIException("This order does not belong to the user");
        }

        shopOrder.setPaymentId(shopOrderUpdateRequest.getPaymentId());
        shopOrder.setPaymentStatus(shopOrderUpdateRequest.getPaymentStatus());
        shopOrder.setPaymentIntentId(shopOrderUpdateRequest.getPaymentIntentId());
        shopOrder.setPaymentTransactionId(shopOrderUpdateRequest.getPaymentTransactionId());
        shopOrder.setPaymentMethodName(shopOrderUpdateRequest.getPaymentMethodName());
        shopOrder.setPaymentCreatedAt(shopOrderUpdateRequest.getPaymentCreatedAt());

        if(shopOrderUpdateRequest.getOrderStatus() != null){
            ShippingMethod shippingMethod = shippingMethodRepository.findByName(shopOrderUpdateRequest.getOrderStatus())
                    .orElseThrow(() -> new NotFoundException("Shipping Method", Optional.of(shopOrderUpdateRequest.getOrderStatus().toString())));
            shopOrder.setShippingMethod(shippingMethod);
        }

        ShopOrder updated = shopOrderRepository.save(shopOrder);
        return convertToShopOrderResponse(updated);
    }

    public ShopOrderResponse convertToShopOrderResponse(ShopOrder order) {
        if (order == null) {
            return null;
        }

        ShippingMethodResponse shippingMethodResponse = null;
        if (order.getShippingMethod() != null) {
            shippingMethodResponse = new ShippingMethodResponse(
                    order.getShippingMethod().getId(),
                    order.getShippingMethod().getName(),
                    order.getShippingMethod().getPrice()
            );
        }

        List<Integer> productItemIds = order.getOrderLines().stream()
                .map(OrderLine::getProductItemId)
                .collect(Collectors.toList());

        List<ProductItemToOrderResponse> productItems = productItemCallerService.getProductItemByIdsToOrders(productItemIds);

        Map<Integer, ProductItemToOrderResponse> productItemMap = productItems.stream()
                .collect(Collectors.toMap(ProductItemToOrderResponse::getId, Function.identity()));


        List<OrderLineResponseWithProductItem> orderLineResponses = order.getOrderLines().stream()
                .map(orderLine -> {
                    ProductItemToOrderResponse productItemToOrderResponse = productItemMap.get(orderLine.getProductItemId());

                    return OrderLineResponseWithProductItem.builder()
                            .id(orderLine.getId())
                            .productName(orderLine.getProductName())
                            .productItem(productItemToOrderResponse)
                            .qty(orderLine.getQty())
                            .price(orderLine.getPrice())
                            .build();
                }).collect(Collectors.toList());

        return ShopOrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderDate(order.getOrderDate())
                .shippingMethod(shippingMethodResponse)
                .orderTotal(order.getOrderTotal())
                .finalOrderTotal(order.getFinalOrderTotal())
                .orderStatus(order.getOrderStatus())
                .orderLines(orderLineResponses)
                .paymentId(order.getPaymentId())
                .appliedDiscountValue(order.getAppliedDiscountValue())
                .paymentTransactionId(order.getPaymentTransactionId())
                .paymentIntentId(order.getPaymentIntentId())
                .paymentMethodName(order.getPaymentMethodName())
                .paymentCreatedAt(order.getPaymentCreatedAt())
                .paymentStatus(order.getPaymentStatus())
                .shippingFirstName(order.getShippingFirstName())
                .shippingLastName(order.getShippingLastName())
                .shippingStreet(order.getShippingStreet())
                .shippingCity(order.getShippingCity())
                .shippingPostalCode(order.getShippingPostalCode())
                .shippingCountry(order.getShippingCountry())
                .shippingPhoneNumber(order.getShippingPhoneNumber())
                .shippingAddressLine1(order.getShippingAddressLine1())
                .shippingAddressLine2(order.getShippingAddressLine2())
                .build();
    }

    public ProductItemToOrderResponse mapProductItem(ProductItemResponse productItem) {
        return ProductItemToOrderResponse.builder()
                .id(productItem.getId())
                .price(productItem.getPrice())
                .discount(productItem.getDiscount())
                .productCode(productItem.getProductCode())
                .qtyInStock(productItem.getQtyInStock())
                .productId(productItem.getProductId())
                .variationOptions(Optional.ofNullable(productItem.getVariationOptions())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(variationOption -> VariationOptionWithVariationResponse.builder()
                                .id(variationOption.getId())
                                .value(variationOption.getValue())
                                .variation(new VariationShortResponse(
                                        variationOption.getVariation().getId(),
                                        variationOption.getVariation().getName()
                                ))
                                .build())
                        .collect(Collectors.toList()))
                .productImages(Optional.ofNullable(productItem.getProductImages())
                        .orElse(Collections.emptySet())
                        .stream()
                        .sorted(Comparator.comparing(ProductImageResponse::getId))
                        .map(image -> ProductImageResponse.builder()
                                .id(image.getId())
                                .imageFilename(image.getImageFilename())
                                .build())
                        .collect(Collectors.toList()))
                .productName(productItem.getProductName())
                .productDescription(productItem.getProductDescription())
                .build();
    }

    private UserResponse getAuthenticatedUser(Jwt jwt) {
        return userCallerService.getUserProfile(jwt);
    }
}
