package com.ecommerce.user.userReview;

import com.cloudinary.api.exceptions.ApiException;
import com.ecommerce.user.clients.ProductCallerService;
import com.ecommerce.user.clients.ShopOrderCallerService;
import com.ecommerce.user.clients.dto.*;
import com.ecommerce.user.exceptions.NotFoundException;
import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserRepository;
import com.ecommerce.user.userReview.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserReviewService {

    private final UserReviewRepository userReviewRepository;
    private final UserRepository userRepository;
    private final ShopOrderCallerService shopOrderCallerService;
    private final ProductCallerService productCallerService;

    @Transactional
    public UserReviewResponse createReview(UserReviewRequest userReviewRequest, Jwt jwt) {

        String currentKeycloakId = jwt.getSubject();
        User user = userRepository.findByKeycloakId(currentKeycloakId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        OrderLineResponse orderLine = shopOrderCallerService.getOrderLineById(userReviewRequest.getOrderLineId(), jwt);

        if (userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), orderLine.getId())) {
            throw new IllegalStateException("You already reviewed this product");
        }

        UserReview review = UserReview.builder()
                .userId(user.getId())
                .orderLineId(orderLine.getId())
                .ratingValue(userReviewRequest.getRatingValue())
                .comment(userReviewRequest.getComment())
                .createdDate(LocalDateTime.now())
                .build();

        userReviewRepository.save(review);

        return UserReviewResponse.builder()
                .id(user.getId())
                .userName(user.getFullName())
                .ratingValue(userReviewRequest.getRatingValue())
                .comment(userReviewRequest.getComment())
                .createdDate(LocalDateTime.now())
                .orderLineId(orderLine.getId())
                .build();
    }

    public List<ProductItemResponse> getProductsForReview(Jwt jwt) {

        String currentKeycloakId = jwt.getSubject();
        User user = userRepository.findByKeycloakId(currentKeycloakId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        List<ShopOrderResponse> orders = shopOrderCallerService.getUserShopOrders(jwt);

        List<ProductItemResponse> productItems = new ArrayList<>();
        for (ShopOrderResponse order : orders) {
            for (OrderLineResponseWithProductItem orderLine : order.getOrderLines()) {
                ProductItemResponse productItem = ProductItemResponse.builder()
                        .id(orderLine.getProductItem().getId())
                        .price(orderLine.getProductItem().getPrice())
                        .discount(orderLine.getProductItem().getDiscount())
                        .productCode(orderLine.getProductItem().getProductCode())
                        .qtyInStock(orderLine.getProductItem().getQtyInStock())
                        .variationOptionIds(
                                orderLine.getProductItem().getVariationOptions().stream()
                                        .map(VariationOptionWithVariationResponse::getId)
                                        .collect(Collectors.toList())
                        )
                        .productImages(Set.copyOf(orderLine.getProductItem().getProductImages()))
                        .build();

                if (!userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), orderLine.getId())) {
                    productItems.add(productItem);
                }
            }
        }

        return productItems;
    }

    public List<ProductItemReviewResponse> getReviewsForProductItems(List<Integer> productItemIds) {

        List<UserReview> reviews = userReviewRepository.findAll();

        List<Integer> orderLineIds = reviews.stream()
                .map(UserReview::getOrderLineId)
                .distinct()
                .collect(Collectors.toList());

        List<OrderLineResponse> orderLines = shopOrderCallerService.getOrderLinesByIds(orderLineIds);

        Map<Integer, Integer> orderLineIdToProductItemId = orderLines.stream()
                .collect(Collectors.toMap(OrderLineResponse::getId, OrderLineResponse::getProductItemId));

        Map<Integer, List<UserReviewResponse>> groupedByProductItem = new HashMap<>();

        for (UserReview review : reviews) {
            Integer orderLineId = review.getOrderLineId();
            Integer productItemId = orderLineIdToProductItemId.get(orderLineId);

            if (productItemId == null || !productItemIds.contains(productItemId)) continue;

            UserReviewResponse response = new UserReviewResponse(
                    review.getId(),
                    review.getUserId(),
                    review.getRatingValue(),
                    review.getComment(),
                    review.getCreatedDate(),
                    orderLineId
            );

            groupedByProductItem
                    .computeIfAbsent(productItemId, k -> new ArrayList<>())
                    .add(response);
        }

        return productItemIds.stream()
                .map(productItemId -> new ProductItemReviewResponse(
                        productItemId,
                        groupedByProductItem.getOrDefault(productItemId, List.of())
                )).collect(Collectors.toList());
    }

    public ProductReviewResponse getReviewForProduct(Integer productId) {

        ProductResponseGetById product = productCallerService.getProductById(productId);

        List<Integer> productItemIds = product.getProductItems()
                .stream()
                .map(ProductItemResponse::getId)
                .toList();

        List<OrderLineResponse> orderLines = shopOrderCallerService
                .getOrderLinesByProductItemsIds(productItemIds);
        List<Integer> orderLineIds = orderLines.stream().map(OrderLineResponse::getId).toList();
        List<UserReview> userReviews = userReviewRepository.findByOrderLineIdIn(orderLineIds);

        List<String> userIds = userReviews.stream()
                .map(UserReview::getUserId)
                .distinct()
                .toList();

        List<User> users = userRepository.findByIdIn(userIds);

        Map<String, String> userIdToName = users.stream().collect(Collectors.toMap(User::getId, User::getFullName));

        List<UserReviewResponse> productReviews = userReviews.stream()
                .map(review -> new UserReviewResponse(
                        review.getId(),
                        userIdToName.getOrDefault(review.getUserId(), "Unknown"),
                        review.getRatingValue(),
                        review.getComment(),
                        review.getCreatedDate(),
                        review.getOrderLineId()
                ))
                .collect(Collectors.toList());

        int total = productReviews.size();
        double average = total == 0 ? 0.0 :
                productReviews.stream().mapToInt(UserReviewResponse::getRatingValue).average().orElse(0.0);

        return new ProductReviewResponse(
                productId,
                productReviews,
                average,
                total
        );
    }

    public List<ProductReviewSummaryResponse> getSummaryReviewForProduct(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        productIds = productIds.stream().filter(Objects::nonNull).distinct().toList();

        Map<Integer, Integer> productItemToProductId = new HashMap<>();

        for (Integer productId : productIds) {
            ProductResponseGetById product = productCallerService.getProductById(productId);

            if (product == null || product.getProductItems() == null) continue;

            product.getProductItems().forEach(pi -> productItemToProductId.put(pi.getId(), productId));
        }

        List<Integer> productItemIds = new ArrayList<>(productItemToProductId.keySet());

        if (productItemIds.isEmpty()) {
            return productIds.stream()
                    .map(id -> new ProductReviewSummaryResponse(id, 0.0, 0))
                    .toList();
        }

        List<OrderLineResponse> orderLines = shopOrderCallerService.getOrderLinesByProductItemsIds(productItemIds);
        if (orderLines.isEmpty()) {
            return productIds.stream()
                    .map(id -> new ProductReviewSummaryResponse(id, 0.0, 0))
                    .toList();
        }

        Map<Integer, List<Integer>> productToOrderLineIds = orderLines.stream()
                .filter(ol -> productItemToProductId.containsKey(ol.getProductItemId()))
                .collect(Collectors.groupingBy(
                        ol -> productItemToProductId.get(ol.getProductItemId()),
                        Collectors.mapping(OrderLineResponse::getId, Collectors.toList())
                ));

        List<Integer> allOrderLineIds = orderLines.stream()
                .map(OrderLineResponse::getId)
                .toList();

        if (allOrderLineIds.isEmpty()) {
            return productIds.stream()
                    .map(id -> new ProductReviewSummaryResponse(id, 0.0, 0))
                    .toList();
        }

        List<UserReview> allReviews = userReviewRepository.findByOrderLineIdIn(allOrderLineIds);


        return productIds.stream().map(productId -> {
            List<Integer> orderLineIds = productToOrderLineIds.getOrDefault(productId, List.of());
            List<UserReview> reviews = allReviews.stream()
                    .filter(r -> orderLineIds.contains(r.getOrderLineId()))
                    .toList();

            double avg = reviews.isEmpty() ? 0.0 :
                    reviews.stream().mapToInt(UserReview::getRatingValue).average().orElse(0.0);

            return new ProductReviewSummaryResponse(productId, avg, reviews.size());
        }).toList();
    }

    public boolean canUserReviewProduct(Jwt jwt, Integer productItemId, Integer orderLineId) {
        String currentKeycloakId = jwt.getSubject();
        User user = userRepository.findByKeycloakId(currentKeycloakId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        boolean hasPurchased = shopOrderCallerService.canUserReviewOrderLine(orderLineId, productItemId, jwt);
        boolean alreadyReviewed = userReviewRepository.existsByUserIdAndOrderLineId(user.getId(), orderLineId);
        return hasPurchased && !alreadyReviewed;
    }


    public Map<Integer, Boolean> canUserReviewProducts(Jwt jwt, List<Integer> orderLineIds) {
        List<OrderLineResponse> orderLines = shopOrderCallerService.getOrderLinesByIds(orderLineIds);
        return orderLines.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        OrderLineResponse::getId,
                        ol -> canUserReviewProduct(jwt, ol.getProductItemId(), ol.getId())
                ));
    }

    public List<UserReviewResponse> getUserReviewByOrderLineId(List<Integer> orderLineIds, String currentKeycloakId) {

        User user = userRepository.findByKeycloakId(currentKeycloakId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        List<UserReview> reviews = userReviewRepository.findByOrderLineIdIn(orderLineIds);

        List<UserReview> filteredReviews = reviews.stream()
                .filter(review -> review.getUserId().equals(user.getId()))
                .toList();

        List<String> userIds = filteredReviews.stream()
                .map(UserReview::getUserId)
                .distinct()
                .toList();
        List<User> users = userRepository.findByIdIn(userIds);

        Map<String, String> userIdToName = users.stream().collect(Collectors.toMap(User::getId, User::getFullName));

        return filteredReviews.stream().map(review -> new UserReviewResponse(
                review.getId(),
                userIdToName.getOrDefault(review.getUserId(), "Unknown"),
                review.getRatingValue(),
                review.getComment(),
                review.getCreatedDate(),
                review.getOrderLineId()
        )).collect(Collectors.toList());
    }

    @Transactional
    public UserReviewResponse editUserReview(String  userReviewId, UserReviewRequest userReviewRequest, String currentKeycloakId) throws ApiException {
        User user = userRepository.findByKeycloakId(currentKeycloakId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        UserReview userReview = userReviewRepository.findById(userReviewId).orElseThrow(() -> new NotFoundException("UserReview", Optional.of(userReviewId)));

        if (!userReview.getUserId().equals(user.getId())) {
            throw new ApiException("You can only edit your own reviews");
        }

        userReview.setRatingValue(userReviewRequest.getRatingValue());
        userReview.setComment(userReviewRequest.getComment());

        UserReview updatedReview = userReviewRepository.save(userReview);

        return UserReviewResponse.builder()
                .id(updatedReview.getId())
                .userName(user.getFullName())
                .ratingValue(updatedReview.getRatingValue())
                .comment(updatedReview.getComment())
                .orderLineId(updatedReview.getOrderLineId())
                .createdDate(updatedReview.getCreatedDate())
                .build();
    }

    @Transactional
    public void deleteUserReview(String userReviewId, String currentKeycloakId) throws ApiException {
        User user = userRepository.findByKeycloakId(currentKeycloakId).orElseThrow(() -> new NotFoundException("User", Optional.empty()));
        UserReview userReview = userReviewRepository.findById(userReviewId).orElseThrow(() -> new NotFoundException("UserReview", Optional.of(userReviewId.toString())));

        if (!userReview.getUserId().equals(user.getId())) {
            throw new ApiException("You can only edit your own reviews");
        }

        userReviewRepository.delete(userReview);
    }

}
