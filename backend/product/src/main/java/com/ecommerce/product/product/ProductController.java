package com.ecommerce.product.product;

import com.ecommerce.product.config.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(@RequestBody ProductCreateRequest productCreateRequest) {

        ProductCreateResponse createdProduct = productService.createProduct(productCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping("/searchByCategory")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "variationIds", required = false) List<Integer> variationIds,
            @RequestParam(name = "variationOptionIds", required = false) List<Integer> variationOptionIds,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortOrder) {

        ProductResponse products = productService.getProducts(categoryId, variationIds, variationOptionIds, pageNumber, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseGetById> getProductById(@PathVariable Integer id) {
        ProductResponseGetById productById = productService.getProductById(id);
        return ResponseEntity.ok(productById);
    }

    @GetMapping("/limit/{numberOfProducts}")
    public ResponseEntity<List<ProductRequest>> getTheNewestProducts(@PathVariable Integer numberOfProducts) {
        List<ProductRequest> products = productService.getTheNewestProducts(numberOfProducts);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/byQuery")
    public ResponseEntity<List<ProductQueryResponse>> getProductsByProductName(@RequestParam(name="query", required = false) String query){
        List<ProductQueryResponse> productQueryResponses = productService.getProductsByQuery(query);
        return ResponseEntity.ok(productQueryResponses);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductCreateResponse> updateProduct(@RequestBody ProductCreateRequest productCreateRequest, @PathVariable Integer productId ){
        ProductCreateResponse updateProduct = productService.updateProduct(productCreateRequest, productId);
        return ResponseEntity.ok(updateProduct);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok("Product has been deleted!");
    }
}
