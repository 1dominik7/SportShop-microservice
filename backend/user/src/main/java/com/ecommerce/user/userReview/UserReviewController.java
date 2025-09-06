package com.ecommerce.user.userReview;

import com.cloudinary.api.exceptions.ApiException;
import com.ecommerce.user.clients.dto.ProductItemResponse;
import com.ecommerce.user.userReview.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
public class UserReviewController {

    private final UserReviewService userReviewService;

    @PostMapping
    public ResponseEntity<UserReviewResponse> createReview(@RequestBody UserReviewRequest userReviewRequest, @AuthenticationPrincipal Jwt jwt) {
        UserReviewResponse userReview = userReviewService.createReview(userReviewRequest, jwt);

        return ResponseEntity.status(HttpStatus.CREATED).body(userReview);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductItemResponse>> getProductsForReview(@AuthenticationPrincipal Jwt jwt) {
        List<ProductItemResponse> productItems = userReviewService.getProductsForReview(jwt);
        return ResponseEntity.ok(productItems);
    }

    @GetMapping("/productItem")
    public ResponseEntity<List<ProductItemReviewResponse>> getProductItemReviews(@RequestParam List<Integer> productItemIds) {
        List<ProductItemReviewResponse> productItemReviewResponses = userReviewService.getReviewsForProductItems(productItemIds);

        return ResponseEntity.ok(productItemReviewResponses);
    }

    @GetMapping("/productById/{productId}")
    public ResponseEntity<ProductReviewResponse> getReviewForProduct(@PathVariable Integer productId){
        ProductReviewResponse productReviewResponse = userReviewService.getReviewForProduct(productId);

        return ResponseEntity.ok(productReviewResponse);
    }

    @GetMapping("/product-summary")
    public ResponseEntity<List<ProductReviewSummaryResponse>> getSummaryReviewForProduct(@RequestParam List<Integer> productIds){
        List<ProductReviewSummaryResponse> result = userReviewService.getSummaryReviewForProduct(productIds);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/products/can-review")
    public ResponseEntity<Map<Integer, Boolean>> canUserReviewProducts(@RequestParam List<Integer> orderLineIds, @AuthenticationPrincipal Jwt jwt) {
//        String currentKeycloakId = jwt.getSubject();
        Map<Integer, Boolean> canReviewMap = userReviewService.canUserReviewProducts(jwt, orderLineIds);

        return ResponseEntity.ok(canReviewMap);
    }

    @GetMapping("/byOrderLines")
    public ResponseEntity<List<UserReviewResponse>> getUserReviewByOrderLineId(@RequestParam List<Integer> orderLineIds,@AuthenticationPrincipal Jwt jwt) {
        String currentKeycloakId = jwt.getSubject();
        List<UserReviewResponse> userReviewResponses = userReviewService.getUserReviewByOrderLineId(orderLineIds,currentKeycloakId);

        return ResponseEntity.ok(userReviewResponses);
    }

    @PutMapping("/{userReviewId}")
    public ResponseEntity<UserReviewResponse> updateReview( @PathVariable String userReviewId,
                                                            @Valid @RequestBody UserReviewRequest userReviewRequest,
                                                            @AuthenticationPrincipal Jwt jwt) throws ApiException {
        String currentKeycloakId = jwt.getSubject();
        UserReviewResponse response = userReviewService.editUserReview(
                userReviewId,
                userReviewRequest,
                currentKeycloakId
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userReviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable String userReviewId,
                             @AuthenticationPrincipal Jwt jwt) throws ApiException {
        String currentKeycloakId = jwt.getSubject();
        userReviewService.deleteUserReview(userReviewId, currentKeycloakId);

        return ResponseEntity.ok("User review has been successfully deleted!");
    }
}
