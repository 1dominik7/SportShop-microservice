package com.ecommerce.order.shopOrder;

import com.ecommerce.order.clients.ProductItemCallerService;
import com.ecommerce.order.clients.UserCallerService;
import com.ecommerce.order.clients.dto.*;
import com.ecommerce.order.exceptions.APIException;
import com.ecommerce.order.exceptions.NotFoundException;
import com.ecommerce.order.orderLine.OrderLine;
import com.ecommerce.order.orderLine.OrderLineRepository;
import com.ecommerce.order.orderLine.OrderLineResponseWithProductItem;
import com.ecommerce.order.orderStatus.OrderStatus;
import com.ecommerce.order.orderStatus.OrderStatusRepository;
import com.ecommerce.order.shippingMethod.ShippingMethod;
import com.ecommerce.order.shippingMethod.ShippingMethodRepository;
import com.ecommerce.order.shippingMethod.ShippingMethodResponse;
import com.ecommerce.order.shopOrder.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopOrderService {

    private final ShopOrderRepository shopOrderRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderLineRepository orderLineRepository;
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
                    .orElseThrow(() -> new NotFoundException("Address",
                            Optional.ofNullable(addressRequest.getId())));
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
                .orElseThrow(() -> new NotFoundException("Shipping method",
                        Optional.of(request.getShippingMethodId().toString())));

        OrderStatus initialStatus = orderStatusRepository.findByStatus("awaiting payment")
                .orElseThrow(() -> new NotFoundException("Initial order status",
                        Optional.of("awaiting payment")));

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
        List<ProductItemOneByColourResponse> products = productItemCallerService
                .getProductItemByIdsToCreateOrder(productItemIds);

        List<ProductItemOneByColour> allProductItems = products.stream()
                .flatMap(p -> p.getProductItemOneByColour().stream())
                .collect(Collectors.toList());

        Map<Integer, ProductItemOneByColour> productMap = allProductItems.stream()
                .collect(Collectors.toMap(
                        ProductItemOneByColour::getId,
                        p -> p,
                        (existing, replacement) -> existing));

        List<OrderLine> orderLines = cart.getShoppingCartItems().stream()
                .map(item -> {
                    ProductItemOneByColour product = productMap.get(item.getProductItemId());
                    if (product == null) {
                        throw new NotFoundException("ProductItem",
                                Optional.of(item.getProductItemId().toString()));
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
        userCallerService.clearUserCart(jwt);
        return convertToShopOrderResponse(savedOrder);
    }

    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Page<ShopOrderResponse> getAllOrders(Pageable pageable, String query, String searchBy) {

        Page<ShopOrder> shopOrder;

        if (query == null || query.isBlank()) {
            shopOrder = shopOrderRepository.findAll(pageable);
        } else if ("orderId".equalsIgnoreCase(searchBy)) {
            try {

                Integer orderId = Integer.parseInt(query);
                shopOrder = shopOrderRepository.findById(orderId, pageable);
            } catch (NumberFormatException e) {
                shopOrder = Page.empty(pageable);
            }
        } else {
            shopOrder = shopOrderRepository.findByUserIdContainingIgnoreCase(query, pageable);
        }

        List<Integer> allProductItemIds = shopOrder.stream()
                .flatMap(order -> order.getOrderLines().stream())
                .map(OrderLine::getProductItemId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductItemToOrderResponse> productItemToOrderResponses =
                productItemCallerService.getProductItemByIdsToOrders(allProductItemIds);

        Map<Integer, ProductItemToOrderResponse> productItemMap = productItemToOrderResponses.stream()
                .collect(Collectors.toMap(ProductItemToOrderResponse::getId, Function.identity()));

        return shopOrder.map(order -> convertToShopOrderResponseWithProductItems(order, productItemMap));
    }

    public ShopOrderResponse getUserShopOrderById(Integer shopOrderId, Jwt jwt) {
        UserResponse user = getAuthenticatedUser(jwt);
        boolean isAdmin = isAdmin(jwt);

        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(() -> new NotFoundException("Order", Optional.of(shopOrderId.toString())));

        if (!isAdmin && !shopOrder.getUserId().equals(user.getId())) {
            throw new APIException("This order does not belong to the user");
        }

        return convertToShopOrderResponse(shopOrder);
    }

    private boolean isAdmin(Jwt jwt) {
        Map<String, Object> resourcesAccess = jwt.getClaim("resource_access");

        if (resourcesAccess != null && resourcesAccess.containsKey("admin-client")) {
            Map<String, Object> adminClient = (Map<String, Object>) resourcesAccess.get("admin-client");
            if (adminClient != null && adminClient.containsKey("roles")) {
                List<String> roles = (List<String>) adminClient.get("roles");
                return roles.contains("ADMIN");
            }
        }

        return false;
    }

    public List<ShopOrderResponse> getUserShopOrdersWithProductItems(Jwt jwt) {
        UserResponse user = getAuthenticatedUser(jwt);

        List<ShopOrder> shopOrders = shopOrderRepository.findByUserId(user.getId());

        List<Integer> productItemIds = shopOrders.stream()
                .flatMap(order -> order.getOrderLines().stream())
                .map(OrderLine::getProductItemId)
                .collect(Collectors.toList());

        List<ProductItemToOrderResponse> productItems = productItemCallerService
                .getProductItemByIdsToOrders(productItemIds);

        Map<Integer, ProductItemToOrderResponse> productItemsMap = productItems.stream()
                .collect(Collectors.toMap(ProductItemToOrderResponse::getId, item -> item));

        List<ShopOrderResponse> orderResponses = shopOrders.stream()
                .sorted(Comparator.comparing(ShopOrder::getId).reversed())
                .map(order -> convertToShopOrderResponseWithProductItems(order, productItemsMap))
                .collect(Collectors.toList());

        return orderResponses;
    }

    public ShopOrderResponse convertToShopOrderResponseWithProductItems(ShopOrder order,
                                                                        Map<Integer, ProductItemToOrderResponse> productItemsMap) {
        if (order == null) {
            return null;
        }

        ShippingMethodResponse shippingMethodResponse = null;
        if (order.getShippingMethod() != null) {
            shippingMethodResponse = new ShippingMethodResponse(
                    order.getShippingMethod().getId(),
                    order.getShippingMethod().getName(),
                    order.getShippingMethod().getPrice());
        }

        List<OrderLineResponseWithProductItem> orderLineResponses = order.getOrderLines().stream()
                .map(orderLine -> {
                    ProductItemToOrderResponse productItemResponse = productItemsMap
                            .get(orderLine.getProductItemId());
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
                                .variation(new VariationShortResponse(option.getId(),
                                        option.getValue()))
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This order does not belong to the user");
        }

        return convertToShopOrderResponse(order);
    }

    public ShopOrderStatisticsResponse getShopOrderIncomesAndTotalOrders() {
        long totalOrders = shopOrderRepository.count();
        Double totalIncomes = shopOrderRepository.getTotalOrderSum();

        return ShopOrderStatisticsResponse.builder()
                .totalIncomes(totalIncomes)
                .totalOrders(totalOrders)
                .build();
    }

    public List<OrderStatusStatisticsResponse> getTopOrderStatuses() {
        List<OrderStatusStatisticsResponse> topStatuses = shopOrderRepository.findTopOrderStatuses(PageRequest.of(0, 3));
        return topStatuses;
    }

    public SalesRatioStatistics getSalesRatio(Integer month, Integer year) {
        YearMonth currentMonth = YearMonth.of(year, month);
        YearMonth lastMonth = currentMonth.minusMonths(1);

        LocalDateTime startOfCurrentMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfCurrentMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        LocalDateTime startOfLastMonth = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        List<ShopOrder> currentMonthOrders = shopOrderRepository
                .findAllByOrderDateBetween(startOfCurrentMonth, endOfCurrentMonth);

        List<ShopOrder> lastMonthOrders = shopOrderRepository
                .findAllByOrderDateBetween(startOfLastMonth, endOfLastMonth);

        List<Double> currentMonthWeeklySales = getWeeklySales(currentMonthOrders, currentMonth);
        List<Double> lastMonthWeeklySales = getWeeklySales(lastMonthOrders, lastMonth);

        return new SalesRatioStatistics(
                currentMonthWeeklySales,
                lastMonthWeeklySales
        );
    }

    private List<Double> getWeeklySales(List<ShopOrder> orders, YearMonth yearMonth) {
        int weeks = 5;
        List<Double> weeklySales = new ArrayList<>(Collections.nCopies(weeks, 0.0));

        for (ShopOrder order : orders) {
            LocalDate date = order.getOrderDate().toLocalDate();
            int weekIndex = (date.getDayOfMonth() - 1) / 7;

            double currentValue = weeklySales.get(weekIndex);
            weeklySales.set(weekIndex, currentValue + order.getFinalOrderTotal());
        }

        return weeklySales;
    }

    public List<LatestSalesProductsResponse> getLatestProductSales(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<OrderLine> orderLines = orderLineRepository.findLatestOrderLines(pageable);

        List<Integer> productItemIds = orderLines.stream()
                .map(ol -> ol.getProductItemId())
                .collect(Collectors.toList());

        List<ProductItemToOrderResponse> productItems = productItemCallerService
                .getProductItemByIdsToOrders(productItemIds);

        Map<Integer, ProductItemToOrderResponse> productMap = productItems.stream()
                .collect(Collectors.toMap(ProductItemToOrderResponse::getId, Function.identity()));

        return orderLines.stream()
                .map(ol -> {
                    ProductItemToOrderResponse productDto = productMap.get(ol.getProductItemId());

                    return new LatestSalesProductsResponse(
                            productDto != null ? productDto.getProductId() : null,
                            ol.getProductItemId(),
                            ol.getProductName(),
                            ol.getShopOrder().getId(),
                            ol.getShopOrder().getOrderDate(),
                            ol.getPrice(),
                            ol.getQty(),
                            ol.getShopOrder().getOrderStatus().getStatus()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<TopProductSalesResponse> getTopProductSales(Integer month, Integer year, Integer limit) {
        Pageable pageable = PageRequest.of(0, limit);
        YearMonth currentMonth = YearMonth.of(year, month);
        LocalDateTime startOfCurrentMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfCurrentMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        List<TopProductSalesDto> stats = orderLineRepository
                .findTopProductItemsBetween(startOfCurrentMonth, endOfCurrentMonth, pageable);

        List<Integer> productItemIds = stats.stream()
                .map(TopProductSalesDto::getProductItemId)
                .toList();

        List<ProductItemToOrderResponse> productItems = productItemCallerService
                .getProductItemByIdsToOrders(productItemIds);

        Map<Integer, ProductItemToOrderResponse> productMap = productItems.stream()
                .collect(Collectors.toMap(ProductItemToOrderResponse::getId, Function.identity()));

        return stats.stream()
                .map(dto -> new TopProductSalesResponse(
                        productMap.get(dto.getProductItemId()),
                        dto.getTotalQuantity()
                ))
                .toList();
    }

    @Transactional
    public ShopOrderResponse updateShopOrderById(Integer shopOrderId,
                                                 ShopOrderPaymentUpdateRequest shopOrderUpdateRequest, Jwt jwt) {
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

        OrderStatus orderStatus = orderStatusRepository.findByStatus(shopOrderUpdateRequest.getOrderStatus())
                .orElseThrow(() -> new NotFoundException("Order Status",
                        Optional.of(shopOrderUpdateRequest.getOrderStatus())));
        shopOrder.setOrderStatus(orderStatus);

        ShopOrder updated = shopOrderRepository.save(shopOrder);
        return convertToShopOrderResponse(updated);
    }

    public String updateShopOrderStatus(String orderStatusName, List<Integer> shopOrderIds) {

        if (shopOrderIds == null || shopOrderIds.isEmpty()) {
            return "No order IDs provided to update.";
        }

        OrderStatus orderStatus = orderStatusRepository.findByStatus(orderStatusName).
                orElseThrow(() -> new NotFoundException("OrderStatus", Optional.of(orderStatusName)));

        List<ShopOrder> shopOrders = shopOrderRepository.findAllById(shopOrderIds);

        for (ShopOrder order : shopOrders) {
            order.setOrderStatus(orderStatus);
        }

        shopOrderRepository.saveAll(shopOrders);

        return "Updated Shop Order with IDs: " + shopOrderIds + " to status: " + orderStatusName;
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
                    order.getShippingMethod().getPrice());
        }

        List<Integer> productItemIds = order.getOrderLines().stream()
                .map(OrderLine::getProductItemId)
                .collect(Collectors.toList());

        List<ProductItemToOrderResponse> productItems = productItemCallerService
                .getProductItemByIdsToOrders(productItemIds);

        Map<Integer, ProductItemToOrderResponse> productItemMap = productItems.stream()
                .collect(Collectors.toMap(ProductItemToOrderResponse::getId, Function.identity()));

        List<OrderLineResponseWithProductItem> orderLineResponses = order.getOrderLines().stream()
                .map(orderLine -> {
                    ProductItemToOrderResponse productItemToOrderResponse = productItemMap
                            .get(orderLine.getProductItemId());

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
                                        variationOption.getVariation()
                                                .getName()))
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
