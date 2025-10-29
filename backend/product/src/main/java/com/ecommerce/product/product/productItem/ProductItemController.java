package com.ecommerce.product.product.productItem;

import com.ecommerce.product.config.AppConstants;
import com.ecommerce.product.product.productItem.request.CreateProductItemRequest;
import com.ecommerce.product.product.productItem.request.ProductItemPageRequest;
import com.ecommerce.product.product.productItem.request.ProductStockUpdateRequest;
import com.ecommerce.product.product.productItem.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("productItems")
public class ProductItemController {

    private final ProductItemService productItemService;
    private final ProductItemRepository productItemRepository;

    @PostMapping("/create")
    public ResponseEntity<ProductItem> createProductItem(@RequestBody CreateProductItemRequest createProductItemRequest
    ) {
        ProductItem newProductItem = productItemService.createProductItem(createProductItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProductItem);
    }

    @GetMapping("/searchByCategory")
    public ResponseEntity<ProductItemDTO> getFilteredProducts(
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "variationIds", required = false) List<Integer> variationIds,
            @RequestParam(name = "variationOptionIds", required = false) List<Integer> variationOptionIds,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder) {

        ProductItemDTO productItems = productItemService.getProductItems(
                categoryId,
                variationIds,
                variationOptionIds,
                pageNumber,
                pageSize, sortBy, sortOrder);

        return ResponseEntity.ok(productItems);
    }

    @GetMapping("/searchByColour")
    public ResponseEntity<ProductItemGroupByColourDTO> getFilteredProductsByColour(
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "variationIds", required = false) List<Integer> variationIds,
            @RequestParam(name = "variationOptionIds", required = false) List<Integer> variationOptionIds,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder,
            @RequestParam(name = "limit", required = false) Optional<Integer> limit
    ) {

        ProductItemGroupByColourDTO productItems = productItemService.getGroupedProductsByColour(
                categoryId,
                variationIds,
                variationOptionIds,
                pageNumber,
                pageSize, sortBy, sortOrder, limit);

        return ResponseEntity.ok(productItems);
    }


    @GetMapping("/filters")
    public ResponseEntity<List<ProductItemFiltersResponse>> getProductItemFilters(
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "variationIds", required = false) List<Integer> variationIds,
            @RequestParam(name = "variationOptionIds", required = false) List<Integer> variationOptionIds,
            @RequestParam(name = "limit", required = false) Optional<Integer> limit) {

        List<ProductItemFiltersResponse> productItemFiltersResponse = productItemService.getProductItemFilters(
                categoryId,
                variationIds,
                variationOptionIds, limit);

        return ResponseEntity.ok(productItemFiltersResponse);
    }

    @GetMapping("/{productItemId}")
    public ResponseEntity<ProductItemOneByColourResponse> getProductItemById(
            @PathVariable Integer productItemId,
            @RequestParam(required = false) String colour) {
        ProductItemOneByColourResponse productItemOneByColourResponse = productItemService.getProductItemById(productItemId, colour);
        return ResponseEntity.ok(productItemOneByColourResponse);
    }

    @GetMapping("/by-product-id-and-colour")
    public ResponseEntity<ProductItemOneByColourResponse> getProductItemByProductIdAndColour(
            @RequestParam Integer productId,
            @RequestParam String colour) {
        ProductItemOneByColourResponse response = productItemService.getProductItemByProductIdAndColour(productId, colour);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/byProductItemIds")
    public ResponseEntity<List<ProductItemOneByColourResponse>> getProductItemByIds(
            @RequestParam List<Integer> productItemIds) {
        List<ProductItemOneByColourResponse> productItemOneByColourResponses = productItemService.getProductItemByIds(productItemIds);
        return ResponseEntity.ok(productItemOneByColourResponses);
    }

    @GetMapping("/by-product-items-ids/shop-order")
    public ResponseEntity<List<ProductItemToOrderResponse>> getProductItemByIdsToShopOrder(
            @RequestParam List<Integer> productItemIds) {
        List<ProductItemToOrderResponse> productItemOneByColourResponses = productItemService.getProductItemByIdsToShopOrder(productItemIds);
        return ResponseEntity.ok(productItemOneByColourResponses);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<ProductItemPageRequest>> getAllProductItems(
            @RequestParam(name = "items", defaultValue = "10", required = false) Integer items,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder) {
        List<ProductItemPageRequest> productItemPageRequest = productItemService.getAllProductItems(items, sortBy, sortOrder);
        return ResponseEntity.ok(productItemPageRequest);
    }

    @DeleteMapping("/delete/{productItemId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer productItemId) {
        productItemService.deleteProductItem(productItemId);
        return ResponseEntity.ok("Product Item has been deleted!");
    }

    @GetMapping("/byId/{productItemId}")
    public ResponseEntity<ProductItemResponseToOrderShop> getProductItemResponseById(
            @PathVariable Integer productItemId) {
        ProductItemResponseToOrderShop productItemResponse = productItemService.getProductItemResponseById(productItemId);
        return ResponseEntity.ok(productItemResponse);
    }

    @GetMapping("/byIds")
    public ResponseEntity<List<ProductItemOneByColourResponse>> getProductItemResponseByIds(
            @RequestParam List<Integer> productItemIds) {
        List<ProductItemOneByColourResponse> productItemResponse = productItemService.getProductItemByIds(productItemIds);
        return ResponseEntity.ok(productItemResponse);
    }

    @GetMapping("/statistics/totalProducts")
    public ResponseEntity<Long> getTotalProductItemsNumber() {
        Long totalProductItems = productItemRepository.count();
        return ResponseEntity.ok(totalProductItems);
    }

    @PutMapping("/update-stock")
    public void updateStock(@RequestBody List<ProductStockUpdateRequest> updateRequests, @AuthenticationPrincipal Jwt jwt){
        productItemService.updateStock(updateRequests);
    }
}
