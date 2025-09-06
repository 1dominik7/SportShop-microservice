package com.ecommerce.product.product;

import com.ecommerce.product.category.Category;
import com.ecommerce.product.category.CategoryRepository;
import com.ecommerce.product.exceptions.NotFoundException;
import com.ecommerce.product.product.productImage.ProductImage;
import com.ecommerce.product.product.productImage.ProductImageRepository;
import com.ecommerce.product.product.productImage.ProductImageResponse;
import com.ecommerce.product.product.productItem.ProductItem;
import com.ecommerce.product.product.productItem.ProductItemRepository;
import com.ecommerce.product.product.productItem.ProductItemService;
import com.ecommerce.product.product.productItem.request.CreateProductItemRequest;
import com.ecommerce.product.product.productItem.response.ProductItemResponse;
import com.ecommerce.product.variation.Variation;
import com.ecommerce.product.variation.VariationOption;
import com.ecommerce.product.variation.VariationOptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private VariationOptionRepository variationOptionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductItemRepository productItemRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void ProductService_CreateProduct_Success() {

        Category category = Category.builder()
                .id(1)
                .categoryName("Shoes")
                .build();

        ProductImage productImage = ProductImage.builder()
                .id(2L)
                .imageFilename("image1.jpg")
                .build();

        ProductItemResponse productItem = ProductItemResponse.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .variationOptionIds(List.of(1))
                .productImages(Set.of(new ProductImageResponse(2L, "image1.jpg")))
                .qtyInStock(10)
                .build();

        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .productName("TEST")
                .categoryId(1)
                .productItems(List.of(productItem))
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .variation(null)
                .value("Shirt")
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .variation(null)
                .value("Sport")
                .build();

        ProductItem productItemEntity = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .variationOptions(List.of(variationOption1, variationOption2))
                .productImages(List.of(productImage))
                .build();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productName("Test Name")
                .productItems(List.of(productItemEntity))
                .category(category)
                .build();

        List<VariationOption> variationOptions = List.of(variationOption1, variationOption2);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(variationOptionRepository.findAllById(List.of(1))).thenReturn(variationOptions);
        when(productImageRepository.findById(2L)).thenReturn(Optional.ofNullable(productImage));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductCreateResponse response = productService.createProduct(productCreateRequest);

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getProductName()).isEqualTo("Test Name");
        assertThat(response.getCategoryId()).isEqualTo(1);
        assertThat(response.getProductItems()).hasSize(1);

        verify(categoryRepository).findById(1);
        verify(variationOptionRepository).findAllById(List.of(1));
        verify(productImageRepository).findById(2L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void ProductService_GetProducts_CategoryAndVariationOptionIdsAreNull() {
        Integer pageNumber = 1;
        Integer pageSize = 1;
        String sortOrder = "asc";
        String sortBy = "id";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        Category category = Category.builder()
                .id(1)
                .categoryName("Shoes")
                .build();

        ProductImage productImage = ProductImage.builder()
                .id(2L)
                .imageFilename("image1.jpg")
                .build();

        Variation variation = Variation.builder()
                .id(1)
                .name("Size")
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .variation(variation)
                .value("M")
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .variation(variation)
                .value("L")
                .build();

        ProductItem productItem = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .variationOptions(List.of(variationOption1, variationOption2))
                .productImages(List.of(productImage))
                .build();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productName("Test Name")
                .productItems(List.of(productItem))
                .category(category)
                .build();

        productItem.setProduct(product);

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(pageDetails)).thenReturn(page);

        ProductResponse response = productService.getProducts(null, null, null, pageNumber, pageSize, sortBy, sortOrder);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        verify(productRepository).findAll(pageDetails);
    }

    @Test
    void ProductService_GetProducts_VariationOptionIdsNotEmpty() {
        Integer pageNumber = 1;
        Integer pageSize = 1;
        String sortOrder = "asc";
        String sortBy = "id";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        Category category = Category.builder()
                .id(1)
                .categoryName("Shoes")
                .build();

        ProductImage productImage = ProductImage.builder()
                .id(2L)
                .imageFilename("image1.jpg")
                .build();

        Variation variation = Variation.builder()
                .id(1)
                .name("Size")
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .variation(variation)
                .value("M")
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .variation(variation)
                .value("L")
                .build();

        ProductItem productItem = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .variationOptions(List.of(variationOption1, variationOption2))
                .productImages(List.of(productImage))
                .build();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productName("Test Name")
                .productItems(List.of(productItem))
                .category(category)
                .build();

        productItem.setProduct(product);

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        List<Integer> variationOptionIds = List.of(1, 2);

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByVariationsAndValues(
                eq(null), eq(List.of(variation.getId())), eq(variationOptionIds), any(Pageable.class)))
                .thenReturn(page);

        ProductResponse response = productService.getProducts(null, List.of(variation.getId()), variationOptionIds, pageNumber, pageSize, sortBy, sortOrder);

        assertThat(response.getContent()).hasSize(1);
        verify(productRepository).findByVariationsAndValues(
                eq(null),
                eq(List.of(variation.getId())),
                eq(variationOptionIds),
                any(Pageable.class));
    }

    @Test
    void ProductService_GetProducts_OnlyCategoryIdProvided() {
        Integer pageNumber = 1;
        Integer pageSize = 1;
        String sortOrder = "asc";
        String sortBy = "id";
        Pageable pageable = PageRequest.of(1, 1, Sort.by("id").ascending());

        Category category = Category.builder()
                .id(1)
                .categoryName("Shoes")
                .build();

        ProductImage productImage = ProductImage.builder()
                .id(2L)
                .imageFilename("image1.jpg")
                .build();

        Variation variation = Variation.builder()
                .id(1)
                .name("Size")
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .variation(variation)
                .value("M")
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .variation(variation)
                .value("L")
                .build();

        ProductItem productItem = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .variationOptions(List.of(variationOption1, variationOption2))
                .productImages(List.of(productImage))
                .build();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productName("Test Name")
                .productItems(List.of(productItem))
                .category(category)
                .build();

        productItem.setProduct(product);

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByCategoryId(5, pageDetails)).thenReturn(page);

        ProductResponse response = productService.getProducts(5, null, null, pageNumber, pageSize, sortBy, sortOrder);

        assertThat(response.getContent()).hasSize(1);
        verify(productRepository).findByCategoryId(5, pageable);
    }

    @Test
    void ProductService_GetTheNewestProducts_Success() {
        Integer pageNumber = 1;
        Integer pageSize = 1;
        String sortOrder = "asc";
        String sortBy = "id";
        Pageable pageable = PageRequest.of(0, 3, Sort.by("createdDate").descending());

        Category category = Category.builder()
                .id(1)
                .categoryName("Shoes")
                .build();

        ProductImage productImage = ProductImage.builder()
                .id(2L)
                .imageFilename("image1.jpg")
                .build();

        Variation variation = Variation.builder()
                .id(1)
                .name("Size")
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .variation(variation)
                .value("M")
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .variation(variation)
                .value("L")
                .build();

        ProductItem productItem = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .variationOptions(List.of(variationOption1, variationOption2))
                .productImages(List.of(productImage))
                .build();

        Product product1 = Product.builder()
                .id(1)
                .description("Test Desc1")
                .productName("Test Name1")
                .productItems(List.of(productItem))
                .category(category)
                .build();

        Product product2 = Product.builder()
                .id(2)
                .description("Test Desc2")
                .productName("Test Name2")
                .productItems(List.of(productItem))
                .category(category)
                .build();

        productItem.setProduct(product1);

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> page = new PageImpl<>(List.of(product1, product2), pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(page);

        List<ProductRequest> result = productService.getTheNewestProducts(3);

        assertThat(result).hasSize(2);
        verify(productRepository).findAll(pageable);
    }

    @Test
    void ProductService_GetProductById_Success() {
        Category category = Category.builder()
                .id(1)
                .categoryName("Shoes")
                .build();

        ProductImage productImage = ProductImage.builder()
                .id(2L)
                .imageFilename("image1.jpg")
                .build();

        Variation variation = Variation.builder()
                .id(1)
                .name("Size")
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .variation(variation)
                .value("M")
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .variation(variation)
                .value("L")
                .build();

        ProductItem productItem = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .variationOptions(List.of(variationOption1, variationOption2))
                .productImages(List.of(productImage))
                .build();

        Product product1 = Product.builder()
                .id(1)
                .description("Test Desc1")
                .productName("Test Name1")
                .productItems(List.of(productItem))
                .category(category)
                .build();

        when(productRepository.findById(1)).thenReturn(Optional.of(product1));

        ProductResponseGetById response = productService.getProductById(1);

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getProductName()).isEqualTo("Test Name1");
        assertThat(response.getCategoryId()).isEqualTo(1);
        verify(productRepository).findById(1);
    }

    @Test
    void ProductService_UpdateProduct_Success() {

        Category category = Category.builder()
                .id(1)
                .categoryName("Shoes")
                .build();

        ProductItemResponse productItem = ProductItemResponse.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .variationOptionIds(List.of(1))
                .qtyInStock(10)
                .build();

        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .productName("Updated")
                .description("Updated")
                .categoryId(1)
                .productItems(List.of(productItem))
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .variation(null)
                .value("Shirt")
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .variation(null)
                .value("Sport")
                .build();

        ProductItem productItemEntity = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .variationOptions(List.of(variationOption1, variationOption2))
                .build();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productName("Test Name")
                .productItems(List.of(productItemEntity))
                .category(category)
                .build();

        productItemEntity.setProduct(product);

        when(variationOptionRepository.findAllById(List.of(1))).thenReturn(List.of(variationOption1));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductCreateResponse response = productService.updateProduct(productCreateRequest, 1);

        assertThat(response).isNotNull();
        assertThat(response.getProductName()).isEqualTo("Updated");
        assertThat(response.getDescription()).isEqualTo("Updated");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void ProductService_UpdateProduct_CategoryIdMissing() {

        ProductItemResponse productItem = ProductItemResponse.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .variationOptionIds(List.of(1))
                .productImages(Set.of(new ProductImageResponse(2L, "image1.jpg")))
                .qtyInStock(10)
                .build();

        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .categoryId(null)
                .productName("Updated")
                .description("Updated")
                .productItems(List.of(productItem))
                .build();

        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(productCreateRequest, 1));
    }

    @Test
    void ProductService_DeleteProduct_Success() {

        Category category = Category.builder()
                .id(1)
                .categoryName("Shoes")
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .variation(null)
                .value("Shirt")
                .productItems(new ArrayList<>())
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .variation(null)
                .value("Sport")
                .productItems(new ArrayList<>())
                .build();

        ProductItem productItemEntity = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .productImages(new ArrayList<>())
                .variationOptions(List.of(variationOption1, variationOption2))
                .build();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productName("Test Name")
                .productItems(List.of(productItemEntity))
                .category(category)
                .build();

        productItemEntity.setProduct(product);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        productService.deleteProduct(1);
        verify(productItemRepository).delete(productItemEntity);
        verify(productRepository).delete(product);
    }

    @Test
    void ProductService_DeleteProduct_NotFound() {
        when(productRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.deleteProduct(1));
    }
    }
