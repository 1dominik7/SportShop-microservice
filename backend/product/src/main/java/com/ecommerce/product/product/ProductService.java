package com.ecommerce.product.product;

import com.ecommerce.product.category.Category;
import com.ecommerce.product.category.CategoryRepository;
import com.ecommerce.product.exceptions.NotFoundException;
import com.ecommerce.product.product.productImage.ProductImage;
import com.ecommerce.product.product.productImage.ProductImageRepository;
import com.ecommerce.product.product.productImage.ProductImageResponse;
import com.ecommerce.product.product.productItem.ProductItem;
import com.ecommerce.product.product.productItem.ProductItemRepository;
import com.ecommerce.product.product.productItem.request.ProductItemRequest;
import com.ecommerce.product.product.productItem.response.ProductItemResponse;
import com.ecommerce.product.variation.Variation;
import com.ecommerce.product.variation.VariationResponse;
import com.ecommerce.product.variation.VariationOption;
import com.ecommerce.product.variation.VariationOptionRepository;
import com.ecommerce.product.variation.VariationOptionResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductItemRepository productItemRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final VariationOptionRepository variationOptionRepository;

    @Transactional
    public ProductCreateResponse createProduct(ProductCreateRequest productCreateRequest) {
        if (productCreateRequest.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is missing in request");
        }

        Category category = categoryRepository.findById(productCreateRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category", Optional.of(productCreateRequest.getCategoryId().toString())));

        Product product = new Product();
        product.setProductName(productCreateRequest.getProductName());
        product.setDescription(productCreateRequest.getDescription());
        product.setCategory(category);

        List<ProductItem> productItems = new ArrayList<>();
        for (ProductItemResponse itemResponse : productCreateRequest.getProductItems()) {
            ProductItem productItem = new ProductItem();
            productItem.setId(itemResponse.getId());
            productItem.setPrice(itemResponse.getPrice());
            productItem.setDiscount(itemResponse.getDiscount());
            productItem.setProductCode(itemResponse.getProductCode());
            productItem.setQtyInStock(itemResponse.getQtyInStock());
            productItem.setProduct(product);

            if (itemResponse.getVariationOptionIds() != null && !itemResponse.getVariationOptionIds().isEmpty()) {
                List<VariationOption> variationOptions = variationOptionRepository.findAllById(itemResponse.getVariationOptionIds());
                if (variationOptions.isEmpty()) {
                    String idsAsString = itemResponse.getVariationOptionIds().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(","));
                    throw new NotFoundException("Variation options", Optional.of(idsAsString));
                }
                productItem.setVariationOptions(variationOptions);
            }

            if (itemResponse.getProductImages() != null && !itemResponse.getProductImages().isEmpty()) {
                List<ProductImage> productImages = new ArrayList<>();
                for (ProductImageResponse imageResponse : itemResponse.getProductImages()) {

                    Optional<ProductImage> existingImage = imageResponse.getId() != null
                            ? productImageRepository.findById(imageResponse.getId())
                            : productImageRepository.findByImageFilename(imageResponse.getImageFilename());

                    if (existingImage.isPresent()) {

                        ProductImage productImage = existingImage.get();
                        productImage.setImageFilename(imageResponse.getImageFilename());
                        productImages.add(productImage);
                    } else {
                        ProductImage productImage = new ProductImage();
                        productImage.setImageFilename(imageResponse.getImageFilename());
                        productImageRepository.save(productImage);
                        productImages.add(productImage);
                    }
                }
                productItem.setProductImages(productImages);
            }
            productItems.add(productItem);
        }

        product.setProductItems(productItems);
        Product savedProduct = productRepository.save(product);

        return ProductCreateResponse.builder()
                .id(savedProduct.getId())
                .productName(savedProduct.getProductName())
                .description(savedProduct.getDescription())
                .categoryId(savedProduct.getCategory().getId())
                .productItems(savedProduct.getProductItems().stream()
                        .map(item -> ProductItemResponse.builder()
                                .id(item.getId())
                                .price(item.getPrice())
                                .discount(item.getDiscount())
                                .productCode(item.getProductCode())
                                .qtyInStock(item.getQtyInStock())
                                .productId(product.getId())
                                .variationOptionIds(item.getVariationOptions().stream()
                                        .map(VariationOption::getId)
                                        .collect(Collectors.toList()))
                                .productImages(item.getProductImages().stream()
                                        .map(image -> new ProductImageResponse(image.getId(), image.getImageFilename()))
                                        .collect(Collectors.toSet()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public ProductResponse getProducts(
            Integer categoryId, List<Integer> variationIds, List<Integer> variationOptionIds,
            Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> productPage;

        if (categoryId == null && (variationOptionIds == null || variationOptionIds.isEmpty())) {
            productPage = productRepository.findAll(pageDetails);
        } else if (variationOptionIds != null && !variationOptionIds.isEmpty()) {
            productPage = productRepository.findByVariationsAndValues(categoryId, variationIds, variationOptionIds, pageDetails);
        } else {
            productPage = productRepository.findByCategoryId(categoryId, pageDetails);
        }

        List<ProductRequest> productRequests = productPage.getContent().stream()
                .map(this::convertToProductRequest)
                .collect(Collectors.toList());

        return ProductResponse.builder()
                .content(productRequests)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .lastPage(productPage.isLast())
                .build();
    }

    public List<ProductRequest> getTheNewestProducts(Integer numberOfProducts) {

        Sort sortByNewest = Sort.by("createdDate").descending();

        Pageable pageDetails = PageRequest.of(0, numberOfProducts, sortByNewest);

        Page<Product> productPage = productRepository.findAll(pageDetails);

        List<ProductRequest> productRequests = productPage.getContent().stream()
                .map(product -> convertToProductRequest(product))
                .collect(Collectors.toList());

        return productRequests;
    }

    private ProductItemRequest convertToProductItemRequest(ProductItem productItem) {
        return ProductItemRequest.builder()
                .id(productItem.getId())
                .price(productItem.getPrice())
                .discount(productItem.getDiscount())
                .productCode(productItem.getProductCode())
                .qtyInStock(productItem.getQtyInStock())
                .variations(productItem.getVariationOptions().stream()
                        .collect(Collectors.groupingBy(variationOption -> variationOption.getVariation().getId()))
                        .entrySet().stream()
                        .map(entry -> {
                            Variation variation = entry.getValue().get(0).getVariation();
                            VariationResponse variationResponse = new VariationResponse();
                            variationResponse.setId(variation.getId());
                            variationResponse.setCategoryId(variation.getCategory() != null ? variation.getCategory().getId() : null);
                            variationResponse.setName(variation.getName());

                            List<VariationOptionResponse> optionResponses = entry.getValue().stream()
                                    .map(option -> new VariationOptionResponse(option.getId(), option.getValue()))
                                    .collect(Collectors.toList());

                            variationResponse.setOptions(optionResponses);
                            return variationResponse;
                        })
                        .collect(Collectors.toList()))
                .productImages(productItem.getProductImages())
                .productId(productItem.getProduct().getId())
                .productName(productItem.getProduct().getProductName())
                .colour(productItem.getVariationOptions().stream().filter(vo -> "colour".equals(vo.getVariation().getName())).map(VariationOption::getValue).findFirst().orElse(null))
                .size(productItem.getVariationOptions().stream().filter(vo -> "size".equals(vo.getVariation().getName())).map(VariationOption::getValue).findFirst().orElse(null))
                .build();
    }

    private ProductRequest convertToProductRequest(Product product) {
        List<ProductItemRequest> productItemRequests = product.getProductItems().stream()
                .map(this::convertToProductItemRequest)
                .collect(Collectors.toList());

        return ProductRequest.builder()
                .id(Optional.ofNullable(product.getId()))
                .productName(product.getProductName())
                .description(product.getDescription())
                .productItems(productItemRequests)
                .categoryId(product.getCategory().getId())
                .createdDate(product.getCreatedDate())
                .build();
    }

    public ProductResponseGetById getProductById(Integer id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product", Optional.of(id.toString())));

        ProductResponseGetById productResponseGetById = ProductResponseGetById.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .productItems(product.getProductItems().stream().map(item ->
                        ProductItemResponse.builder()
                                .id(item.getId())
                                .productId(product.getId())
                                .price(item.getPrice())
                                .discount(item.getDiscount())
                                .productCode(item.getProductCode())
                                .qtyInStock(item.getQtyInStock())
                                .variationOptionIds(item.getVariationOptions().stream().map(option ->
                                        option.getId()).collect(Collectors.toList()))
                                .productImages(item.getProductImages().stream()
                                        .map(image -> new ProductImageResponse(image.getId(), image.getImageFilename()))
                                        .collect(Collectors.toSet()))
                                .build()).collect(Collectors.toList())).build();

        return productResponseGetById;
    }

    @Transactional
    public ProductCreateResponse updateProduct(ProductCreateRequest productCreateRequest, Integer productId) {
        if (productCreateRequest.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is missing in request");
        }

        Category category = categoryRepository.findById(productCreateRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category", Optional.of(productCreateRequest.getCategoryId().toString())));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", Optional.of(productId.toString())));

        product.setProductName(productCreateRequest.getProductName());
        product.setDescription(productCreateRequest.getDescription());
        product.setCategory(category);

        Set<Integer> updatedProductItemIds = productCreateRequest.getProductItems().stream()
                .map(ProductItemResponse::getId)
                .collect(Collectors.toSet());

        List<ProductItem> productItemsToRemove = product.getProductItems().stream()
                .filter(item -> !updatedProductItemIds.contains(item.getId()))
                .collect(Collectors.toList());

        removeProductItems(productItemsToRemove);

        List<ProductItem> productItems = new ArrayList<>();
        for (ProductItemResponse itemResponse : productCreateRequest.getProductItems()) {
            ProductItem productItem = product.getProductItems().stream()
                    .filter(item -> item.getId().equals(itemResponse.getId()))
                    .findFirst()
                    .orElse(new ProductItem());

            updateProductItem(productItem, itemResponse, product);
            productItems.add(productItem);
        }

        product.setProductItems(productItems);

        Product savedProduct = productRepository.save(product);
        cleanupUnusedImages();

        return buildProductCreateResponse(savedProduct);
    }

    private void removeProductItems(List<ProductItem> productItemsToRemove) {
        for (ProductItem productItem : productItemsToRemove) {
            if (productItem.getProduct() != null) {
                productItem.getProduct().getProductItems().remove(productItem);
            }

            if (productItem.getVariationOptions() != null) {
                for (VariationOption variationOption : productItem.getVariationOptions()) {
                    variationOption.getProductItems().remove(productItem);
                }
            }

            if (productItem.getProductImages() != null) {
                for (ProductImage image : productItem.getProductImages()) {
                    productImageRepository.delete(image);
                }
            }

            productItemRepository.delete(productItem);
        }
    }

    private void cleanupUnusedImages() {

        List<ProductImage> allImages = productImageRepository.findAll();

        for (ProductImage image : allImages) {
            if (image.getProductItems().isEmpty()) {
                productImageRepository.delete(image);
            }
        }
    }

    private void updateProductItem(ProductItem productItem, ProductItemResponse itemResponse, Product product) {
        productItem.setPrice(itemResponse.getPrice());
        productItem.setDiscount(itemResponse.getDiscount());
        productItem.setProductCode(itemResponse.getProductCode());
        productItem.setQtyInStock(itemResponse.getQtyInStock());
        productItem.setProduct(product);

        processVariationOptions(itemResponse, productItem);
        updateProductImages(itemResponse, productItem);
    }

    private void processVariationOptions(ProductItemResponse itemResponse, ProductItem productItem) {
        if (itemResponse.getVariationOptionIds() != null && !itemResponse.getVariationOptionIds().isEmpty()) {
            List<VariationOption> variationOptions = variationOptionRepository.findAllById(itemResponse.getVariationOptionIds());
            if (variationOptions.isEmpty()) {
                throw new NotFoundException("Variation options", Optional.empty());
            }
            productItem.setVariationOptions(variationOptions);
        }
    }

    private void updateProductImages(ProductItemResponse itemResponse, ProductItem productItem) {
        if (itemResponse.getProductImages() != null && !itemResponse.getProductImages().isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();

            for (ProductImageResponse imageResponse : itemResponse.getProductImages()) {
                String filename = imageResponse.getImageFilename();

                if (filename == null || filename.startsWith("blob:")) {
                    continue;
                }

                Optional<ProductImage> existingImage = productImageRepository.findByImageFilename(filename);

                ProductImage productImage = existingImage.orElseGet(() -> {
                    ProductImage newImage = new ProductImage();
                    newImage.setImageFilename(filename);
                    return productImageRepository.save(newImage);
                });

                productImages.add(productImage);
            }
            productItem.setProductImages(productImages);
        } else {

            productItem.setProductImages(new ArrayList<>());
        }
    }

    private ProductCreateResponse buildProductCreateResponse(Product savedProduct) {
        return ProductCreateResponse.builder()
                .id(savedProduct.getId())
                .productName(savedProduct.getProductName())
                .description(savedProduct.getDescription())
                .categoryId(savedProduct.getCategory().getId())
                .productItems(savedProduct.getProductItems().stream()
                        .map(item -> ProductItemResponse.builder()
                                .id(item.getId())
                                .price(item.getPrice())
                                .discount(item.getDiscount())
                                .productCode(item.getProductCode())
                                .qtyInStock(item.getQtyInStock())
                                .productId(savedProduct.getId())
                                .variationOptionIds(item.getVariationOptions().stream()
                                        .map(VariationOption::getId)
                                        .collect(Collectors.toList()))
                                .productImages(item.getProductImages().stream()
                                        .map(image -> new ProductImageResponse(image.getId(), image.getImageFilename()))
                                        .collect(Collectors.toSet()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product", Optional.of(productId.toString())));

        if (!product.getProductItems().isEmpty()) {
            for (ProductItem productItem : product.getProductItems()) {

                if (!productItem.getProductImages().isEmpty()) {
                    Set<ProductImage> imagesToRemove = new HashSet<>(productItem.getProductImages());
                    for (ProductImage productImage : imagesToRemove) {

                        productImage.getProductItems().remove(productItem);
                        productItem.getProductImages().remove(productImage);

                        if (productImage.getProductItems().isEmpty()) {
                            productImageRepository.delete(productImage);
                        }
                    }
                }

                if (!productItem.getVariationOptions().isEmpty()) {
                    for (VariationOption variationOption : productItem.getVariationOptions()) {
                        variationOption.getProductItems().remove(productItem);
                    }
                }

                productItemRepository.delete(productItem);
            }
        }

        productRepository.delete(product);
    }
}
