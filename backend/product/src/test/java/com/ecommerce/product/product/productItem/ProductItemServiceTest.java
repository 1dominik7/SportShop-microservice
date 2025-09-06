package com.ecommerce.product.product.productItem;

import com.ecommerce.product.category.Category;
import com.ecommerce.product.category.CategoryRepository;
import com.ecommerce.product.category.CategoryRequest;
import com.ecommerce.product.category.CategoryResponse;
import com.ecommerce.product.exceptions.APIException;
import com.ecommerce.product.product.Product;
import com.ecommerce.product.product.ProductRepository;
import com.ecommerce.product.product.productImage.ProductImage;
import com.ecommerce.product.product.productItem.request.CreateProductItemRequest;
import com.ecommerce.product.product.productItem.request.ProductItemRequest;
import com.ecommerce.product.product.productItem.request.ProductStockUpdateRequest;
import com.ecommerce.product.product.productItem.response.ProductItemFiltersResponse;
import com.ecommerce.product.product.productItem.response.ProductItemGroupByColorResponse;
import com.ecommerce.product.product.productItem.response.ProductItemGroupByColourDTO;
import com.ecommerce.product.product.productItem.response.ProductItemOneByColourResponse;
import com.ecommerce.product.variation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductItemServiceTest {

    @Mock
    private VariationOptionRepository variationOptionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductItemRepository productItemRepository;


    @InjectMocks
    private ProductItemService productItemService;


    @Test
    void ProductItemService_CreateProductItem_Success() {

        CreateProductItemRequest productItemRequest = CreateProductItemRequest.builder()
                .productId(1)
                .productCode("TEST")
                .price(100.00)
                .qtyInStock(10)
                .variationOptionIds(List.of(1, 2))
                .productImages(null)
                .build();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productItems(List.of())
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

        List<VariationOption> variationOptions = List.of(variationOption1, variationOption2);

        ProductItem expectedProductItem = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        when(productRepository.findById(eq(productItemRequest.getProductId())))
                .thenReturn(Optional.ofNullable(product));
        when(variationOptionRepository.findAllById(eq(productItemRequest.getVariationOptionIds())))
                .thenReturn(variationOptions);
        when(productItemRepository.save(any(ProductItem.class))).thenReturn(expectedProductItem);

        ProductItem result = productItemService.createProductItem(productItemRequest);

        assertNotNull(result);
        assertNotNull(result);
        assertEquals("TEST", result.getProductCode());
        assertEquals(100.0, result.getPrice());
        ;
        assertEquals(2, result.getVariationOptions().size());
        assertEquals(product, result.getProduct());


        verify(productRepository).findById(1);
        verify(variationOptionRepository).findAllById(List.of(1, 2));
        verify(productItemRepository).save(any(ProductItem.class));
    }

    @Test
    void ProductItemService_GetProductItems_Success() {
        Integer categoryId = 1;
        List<Integer> variationIds = List.of(1, 2);
        List<Integer> variationOptionIds = List.of(1, 2);
        int pageNumber = 0;
        int pageSize = 2;
        String sortBy = "id";
        String sortOrder = "asc";

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productItems(List.of())
                .build();

        Variation variation1 = Variation.builder()
                .id(1)
                .name("Size")
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .variation(variation1)
                .value("S")
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .variation(variation1)
                .value("M")
                .build();

        List<VariationOption> variationOptions = List.of(variationOption1, variationOption2);

        ProductItem productItem = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        when(productItemRepository.findByFilters(categoryId, variationIds, variationOptionIds))
                .thenReturn(new ArrayList<>(List.of(productItem)));

        ProductItemDTO result = productItemService.getProductItems(categoryId, variationIds, variationOptionIds,
                pageNumber, pageSize, sortBy, sortOrder);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLastPage());

        verify(productItemRepository).findByFilters(categoryId, variationIds, variationOptionIds);
    }

    @Test
    void ProductItemService_GetProductItems_NoFilters() {
        Integer categoryId = 1;
        List<Integer> variationIds = null;
        List<Integer> variationOptionIds = null;
        int pageNumber = 0;
        int pageSize = 2;
        String sortBy = "id";
        String sortOrder = "asc";

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productItems(List.of())
                .build();

        Variation variation1 = Variation.builder()
                .id(1)
                .name("Size")
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .variation(variation1)
                .value("S")
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .variation(variation1)
                .value("M")
                .build();

        List<VariationOption> variationOptions = List.of(variationOption1, variationOption2);

        ProductItem productItem = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        when(productItemRepository.findByCategoryId(categoryId))
                .thenReturn(new ArrayList<>(List.of(productItem)));

        ProductItemDTO result = productItemService.getProductItems(categoryId, variationIds, variationOptionIds,
                pageNumber, pageSize, sortBy, sortOrder);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isLastPage());

        verify(productItemRepository).findByCategoryId(categoryId);
        verify(productItemRepository, never()).findAll();
        verify(productItemRepository, never()).findByFilters(any(), any(), any());
    }

    @Test
    void ProductItemService_GetGroupedProductsByColour_NoFilters() {
        Integer categoryId = 1;
        List<Integer> variationIds = List.of(1, 2);
        List<Integer> variationOptionIds = List.of(1, 2);
        int pageNumber = 0;
        int pageSize = 2;
        String sortBy = "id";
        String sortOrder = "asc";
        Optional<Integer> limit = Optional.empty();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productItems(List.of())
                .build();

        Variation variation1 = Variation.builder()
                .id(1)
                .name("Colour")
                .build();

        VariationOption variationOption = VariationOption.builder()
                .id(1)
                .variation(variation1)
                .value("Red")
                .build();

        ProductItem productItem = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .product(product)
                .variationOptions(List.of(variationOption))
                .productImages(List.of())
                .build();

        when(productItemRepository.findByFilters(categoryId, variationIds, variationOptionIds))
                .thenReturn(new ArrayList<>(List.of(productItem)));

        ProductItemGroupByColourDTO result = productItemService.getGroupedProductsByColour(categoryId, variationIds, variationOptionIds, pageNumber, pageSize, sortBy, sortOrder, limit);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Red", result.getContent().get(0).getColour());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        verify(productItemRepository).findByFilters(categoryId, variationIds, variationOptionIds);
    }

    @Test
    void ProductItemService_GetProductItemFilters_Success() {
        Integer categoryId = 1;
        List<Integer> variationIds = List.of(1, 2);
        List<Integer> variationOptionIds = List.of(1, 2);
        Optional<Integer> limit = Optional.empty();

        VariationOptionResponse variationOption1 = VariationOptionResponse.builder()
                .id(1)
                .value("Red")
                .build();

        VariationOptionResponse variationOption2 = VariationOptionResponse.builder()
                .id(2)
                .value("Blue")
                .build();

        VariationResponse variation = VariationResponse.builder()
                .id(1)
                .name("Colour")
                .options(List.of(variationOption1, variationOption2))
                .build();

        ProductItemRequest itemRequest = ProductItemRequest.builder()
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .productId(1)
                .variations(List.of(variation))
                .productImages(List.of())
                .build();

        ProductItemGroupByColorResponse colorGroup = ProductItemGroupByColorResponse.builder()
                .productId(1)
                .colour("Red")
                .productItemRequests(List.of(itemRequest))
                .build();

        ProductItemGroupByColourDTO groupedProducts = ProductItemGroupByColourDTO.builder()
                .content(List.of(colorGroup))
                .pageSize(1)
                .pageNumber(0)
                .totalElements(1)
                .totalPages(1)
                .lastPage(true)
                .build();

        ProductItemService spyService = spy(productItemService);
        doReturn(groupedProducts).when(spyService).getGroupedProductsByColour(eq(categoryId), eq(variationIds), eq(variationOptionIds), eq(0), eq(Integer.MAX_VALUE),
                eq("productId"), eq("asc"), eq(limit));

        List<ProductItemFiltersResponse> filters = spyService.getProductItemFilters(categoryId, variationIds, variationOptionIds, limit);

        assertNotNull(filters);
        assertEquals(1, filters.size());

        ProductItemFiltersResponse response = filters.get(0);
        assertEquals(categoryId, response.getCategoryId());
        assertEquals("Colour", response.getVariation().getName());
        assertEquals(2, response.getVariation().getOptions().size());
        assertTrue(response.getVariation().getOptions().stream().anyMatch(o -> o.getValue().equals("Red")));
        assertTrue(response.getVariation().getOptions().stream().anyMatch(o -> o.getValue().equals("Blue")));
    }

    @Test
    void ProductItemService_GetProductItemById_Success() {
        Integer productItemId = 1;
        String colour = "Red";

        Category category = Category.builder()
                .id(1)
                .categoryName("Test Category")
                .build();


        Variation colourVariation = Variation.builder()
                .id(10)
                .name("Colour")
                .category(category)
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .value("Red")
                .variation(colourVariation)
                .build();

        VariationOption variationOption2 = VariationOption.builder()
                .id(2)
                .value("Blue")
                .variation(colourVariation)
                .build();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productItems(List.of())
                .category(category)
                .build();

        List<VariationOption> variationOptions = List.of(variationOption1, variationOption2);

        ProductItem productItem1 = ProductItem.builder()
                .id(1)
                .productCode("TEST-1")
                .price(100.0)
                .qtyInStock(10)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        ProductItem productItem2 = ProductItem.builder()
                .id(2)
                .productCode("TEST-2")
                .price(90.0)
                .qtyInStock(5)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        when(productItemRepository.findById(productItemId)).thenReturn(Optional.of(productItem1));
        when(productItemRepository.findByProductId(product.getId())).thenReturn(List.of(productItem1, productItem2));

        ProductItemOneByColourResponse response = productItemService.getProductItemById(productItemId, colour);

        assertEquals(colour.toLowerCase(), response.getColour().toLowerCase());
        assertEquals(product.getId(), response.getProductId());
        assertEquals(productItemId, response.getProductItemId());
        assertEquals(product.getProductName(), response.getProductName());
        assertFalse(response.getProductItemOneByColour().isEmpty());
    }

    @Test
    void ProductItemService_GetProductItemByProductIdAndColour_Success() {
        Integer productId = 1;
        String colour = "Red";

        Category category = Category.builder()
                .id(1)
                .categoryName("Test Category")
                .build();


        Variation colourVariation = Variation.builder()
                .id(10)
                .name("Colour")
                .category(category)
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .value("Red")
                .variation(colourVariation)
                .build();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productItems(List.of())
                .category(category)
                .build();

        List<VariationOption> variationOptions = List.of(variationOption1);

        ProductItem productItem1 = ProductItem.builder()
                .id(1)
                .productCode("TEST-1")
                .price(100.0)
                .qtyInStock(10)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        ProductItem productItem2 = ProductItem.builder()
                .id(2)
                .productCode("TEST-2")
                .price(90.0)
                .qtyInStock(5)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        when(productItemRepository.findByProductId(productId)).thenReturn(List.of(productItem1, productItem2));

        ProductItemOneByColourResponse result = productItemService.getProductItemByProductIdAndColour(productId, colour);

        assertEquals(colour.toLowerCase(), result.getColour().toLowerCase());
        assertEquals(productId, result.getProductId());
        assertEquals(2, result.getProductItemOneByColour().size());

        verify(productItemRepository).findByProductId(productId);
    }

    @Test
    void ProductItemService_GetProductItemByIds_Success() {

        Category category = Category.builder()
                .id(1)
                .categoryName("Test Category")
                .build();


        Variation colourVariation = Variation.builder()
                .id(10)
                .name("Colour")
                .category(category)
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .value("Red")
                .variation(colourVariation)
                .build();

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productItems(List.of())
                .category(category)
                .build();

        List<VariationOption> variationOptions = List.of(variationOption1);

        ProductItem productItem1 = ProductItem.builder()
                .id(1)
                .productCode("TEST-1")
                .price(100.0)
                .qtyInStock(10)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        ProductItem productItem2 = ProductItem.builder()
                .id(2)
                .productCode("TEST-2")
                .price(90.0)
                .qtyInStock(5)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        when(productItemRepository.findAllById(eq(List.of(productItem1.getId(), productItem2.getId())))).thenReturn(List.of(productItem1, productItem2));

        List<ProductItemOneByColourResponse> result = productItemService.getProductItemByIds(List.of(productItem1.getId(), productItem2.getId()));

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductItemId());
        assertEquals("Red", result.get(0).getColour());
        assertEquals(2, result.get(1).getProductItemId());
        assertEquals("Red", result.get(1).getColour());
        assertEquals(2, result.size());

        verify(productItemRepository).findAllById(List.of(productItem1.getId(), productItem2.getId()));
    }

    @Test
    void ProductItemService_DeleteProductItem_Success() {
        Integer productItemId = 1;

        Category category = Category.builder()
                .id(1)
                .categoryName("Test Category")
                .build();


        Variation colourVariation = Variation.builder()
                .id(10)
                .name("Colour")
                .category(category)
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .value("Red")
                .variation(colourVariation)
                .build();

        List<VariationOption> variationOptions = List.of(variationOption1);

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productItems(new ArrayList<>())
                .category(category)
                .build();

        ProductItem productItem1 = ProductItem.builder()
                .id(1)
                .productCode("TEST-1")
                .price(100.0)
                .qtyInStock(10)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        product.getProductItems().add(productItem1);
        variationOption1.setProductItems(new ArrayList<>(List.of(productItem1)));

        when(productItemRepository.findById(eq(productItemId))).thenReturn(Optional.of(productItem1));

        productItemService.deleteProductItem(productItemId);

        assertFalse(product.getProductItems().contains(productItem1));
        assertFalse(variationOption1.getProductItems().contains(productItem1));
        verify(productItemRepository).delete(productItem1);
    }

    @Test
    void ProductItemService_UpdateStock_Success() {
        Integer productItemId = 1;

        Category category = Category.builder()
                .id(1)
                .categoryName("Test Category")
                .build();


        Variation colourVariation = Variation.builder()
                .id(10)
                .name("Colour")
                .category(category)
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .value("Red")
                .variation(colourVariation)
                .build();

        List<VariationOption> variationOptions = List.of(variationOption1);

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productItems(new ArrayList<>())
                .category(category)
                .build();

        ProductItem productItem1 = ProductItem.builder()
                .id(1)
                .productCode("TEST-1")
                .price(100.0)
                .qtyInStock(10)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        ProductStockUpdateRequest updateRequest = ProductStockUpdateRequest.builder()
                .productItemId(1)
                .quantityToSubtract(5)
                .build();

        when(productItemRepository.findById(eq(productItemId))).thenReturn(Optional.of(productItem1));
        when(productItemRepository.save(any(ProductItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productItemService.updateStock(List.of(updateRequest));


        assertEquals(5, productItem1.getQtyInStock());
        verify(productItemRepository).save(productItem1);
    }

    @Test
    void ProductItemService_UpdateStock_WhenNotEnoughStock() {
        Integer productItemId = 1;

        Category category = Category.builder()
                .id(1)
                .categoryName("Test Category")
                .build();

        Variation colourVariation = Variation.builder()
                .id(10)
                .name("Colour")
                .category(category)
                .build();

        VariationOption variationOption1 = VariationOption.builder()
                .id(1)
                .value("Red")
                .variation(colourVariation)
                .build();

        List<VariationOption> variationOptions = List.of(variationOption1);

        Product product = Product.builder()
                .id(1)
                .description("Test Desc")
                .productItems(new ArrayList<>())
                .category(category)
                .build();

        ProductItem productItem1 = ProductItem.builder()
                .id(1)
                .productCode("TEST-1")
                .price(100.0)
                .qtyInStock(10)
                .product(product)
                .variationOptions(variationOptions)
                .productImages(List.of())
                .build();

        ProductStockUpdateRequest updateRequest = ProductStockUpdateRequest.builder()
                .productItemId(1)
                .quantityToSubtract(19)
                .build();

        when(productItemRepository.findById(eq(productItemId))).thenReturn(Optional.of(productItem1));

        APIException ex = assertThrows(APIException.class, () -> {
            productItemService.updateStock(List.of(updateRequest));
        });

        assertTrue(ex.getMessage().contains("Not enough stock"));
        verify(productItemRepository, never()).save(any());
    }
}

