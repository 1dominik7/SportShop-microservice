package com.ecommerce.product.product.productItem;


import com.ecommerce.product.product.productItem.request.CreateProductItemRequest;
import com.ecommerce.product.product.productItem.request.ProductItemPageRequest;
import com.ecommerce.product.product.productItem.request.ProductItemRequest;
import com.ecommerce.product.product.productItem.request.ProductStockUpdateRequest;
import com.ecommerce.product.product.productItem.response.*;
import com.ecommerce.product.variation.VariationOptionController;
import com.ecommerce.product.variation.VariationOptionResponse;
import com.ecommerce.product.variation.VariationOptionService;
import com.ecommerce.product.variation.VariationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {
        ProductItemController.class,
})
@AutoConfigureMockMvc
@EnableWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb"
})
public class ProductItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductItemService productItemService;


    @Test
    void ProductItemController_CreateProductItem_Success() throws Exception {
        CreateProductItemRequest request = CreateProductItemRequest.builder()
                .productId(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .variationOptionIds(List.of(1, 2))
                .build();

        ProductItem response = ProductItem.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .build();

        when(productItemService.createProductItem(any())).thenReturn(response);

        mockMvc.perform(post("/productItems/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productCode").value("TEST"));
    }

    @Test
    void ProductItemController_GetFilteredProducts_Success() throws Exception {

        ProductItemRequest productItem = ProductItemRequest.builder()
                .id(1)
                .productCode("TEST")
                .price(100.0)
                .qtyInStock(10)
                .build();

        ProductItemDTO response = ProductItemDTO.builder()
                .content(List.of(productItem))
                .pageSize(10)
                .pageNumber(0)
                .totalElements(1L)
                .totalPages(1)
                .lastPage(true)
                .build();

        when(productItemService.getProductItems(
                any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/productItems/searchByCategory")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("categoryId", "1")
                        .param("variationIds", "1", "2")
                        .param("variationOptionIds", "3", "4")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "price")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk());
    }

    @Test
    void ProductItemController_GetFilteredProductsByColour_Success() throws Exception {

        ProductItemGroupByColorResponse productItem = ProductItemGroupByColorResponse.builder()
                .colour("Red")
                .productId(1)
                .productItemRequests(List.of())
                .build();

        ProductItemGroupByColourDTO response = ProductItemGroupByColourDTO.builder()
                .content(List.of(productItem))
                .pageSize(10)
                .pageNumber(0)
                .totalElements(1L)
                .totalPages(1)
                .lastPage(true)
                .build();

        when(productItemService.getGroupedProductsByColour(
                any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), any()))
                .thenReturn(response);


        mockMvc.perform(get("/productItems/searchByColour")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("categoryId", "1")
                        .param("variationIds", "1", "2")
                        .param("variationOptionIds", "3", "4")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("sortBy", "price")
                        .param("sortOrder", "desc")
                        .param("limit", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void ProductItemController_GetProductItemFilters_Success() throws Exception {

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

        ProductItemFiltersResponse filters = ProductItemFiltersResponse.builder()
                .categoryId(1)
                .variation(variation)
                .build();

        when(productItemService.getProductItemFilters(
                any(), any(), any(), any()))
                .thenReturn(List.of(filters));


        mockMvc.perform(get("/productItems/filters")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("categoryId", "1")
                        .param("variationIds", "1")
                        .param("variationOptionIds", "1", "2")
                        .param("limit", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void ProductItemController_GetProductItemById_Success() throws Exception {

        ProductItemOneByColourResponse filters = ProductItemOneByColourResponse.builder()
                .productItemId(1)
                .colour("Red")
                .productId(1)
                .build();

        when(productItemService.getProductItemById(eq(1), eq("red")))
                .thenReturn(filters);

        mockMvc.perform(get("/productItems/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("colour", "red"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productItemId").value(1))
                .andExpect(jsonPath("$.colour").value("Red"))
                .andExpect(jsonPath("$.productId").value(1));
    }

    @Test
    void ProductItemController_GetProductItemByProductIdAndColour_Success() throws Exception {

        ProductItemOneByColourResponse filters = ProductItemOneByColourResponse.builder()
                .productItemId(1)
                .colour("Red")
                .productId(1)
                .build();

        when(productItemService.getProductItemByProductIdAndColour(eq(1), eq("Red")))
                .thenReturn(filters);

        mockMvc.perform(get("/productItems/by-product-id-and-colour")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("productId", "1")
                        .param("colour", "Red"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.colour").value("Red"));
    }

    @Test
    void ProductItemController_GetProductItemByIds_Success() throws Exception {

        ProductItemOneByColourResponse productItem1 = ProductItemOneByColourResponse.builder()
                .productItemId(1)
                .colour("Red")
                .productId(1)
                .build();

        ProductItemOneByColourResponse productItem2 = ProductItemOneByColourResponse.builder()
                .productItemId(2)
                .colour("Blue")
                .productId(1)
                .build();

        when(productItemService.getProductItemByIds(any()))
                .thenReturn(List.of(productItem1, productItem2));

        mockMvc.perform(get("/productItems/byProductItemIds")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("productItemIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productItemId").value(1))
                .andExpect(jsonPath("$[0].colour").value("Red"))
                .andExpect(jsonPath("$[1].productItemId").value(2))
                .andExpect(jsonPath("$[1].colour").value("Blue"));
    }

    @Test
    void ProductItemController_GetProductItemByIdsToShopOrder_Success() throws Exception {

        ProductItemToOrderResponse productItem1 = ProductItemToOrderResponse.builder()
                .productId(1)
                .id(1)
                .productCode("PI1")
                .build();

        ProductItemToOrderResponse productItem2 = ProductItemToOrderResponse.builder()
                .productId(1)
                .id(2)
                .productCode("PI2")
                .build();

        when(productItemService.getProductItemByIdsToShopOrder(any()))
                .thenReturn(List.of(productItem1, productItem2));

        mockMvc.perform(get("/productItems/by-product-items-ids/shop-order")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("productItemIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productCode").value("PI1"))
                .andExpect(jsonPath("$[0].productId").value("1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].productCode").value("PI2"))
                .andExpect(jsonPath("$[0].productId").value("1"));
    }

    @Test
    void ProductItemController_GetAllProductItems_Success() throws Exception {

        ProductItemPageRequest productItem1 = ProductItemPageRequest.builder()
                .id(1)
                .productCode("PI1")
                .price(100.0)
                .qtyInStock(1)
                .build();

        ProductItemPageRequest productItem2 = ProductItemPageRequest.builder()
                .id(2)
                .productCode("PI2")
                .price(90.0)
                .qtyInStock(2)
                .build();

        when(productItemService.getAllProductItems(eq(10), any(), any()))
                .thenReturn(List.of(productItem1, productItem2));

        mockMvc.perform(get("/productItems/getAll")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .param("items", "10")
                        .param("sortBy", "id")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].productCode").value("PI1"))
                .andExpect(jsonPath("$[0].price").value("100.0"))
                .andExpect(jsonPath("$[0].qtyInStock").value("1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].productCode").value("PI2"))
                .andExpect(jsonPath("$[1].price").value("90.0"))
                .andExpect(jsonPath("$[1].qtyInStock").value("2"));
    }

    @Test
    void ProductItemController_DeleteProduct_Success() throws Exception {

        doNothing().when(productItemService).deleteProductItem(1);

        mockMvc.perform(delete("/productItems/delete/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Product Item has been deleted!"));
    }

    @Test
    void ProductItemController_GetProductItemResponseById_Success() throws Exception {

        ProductItemResponseToOrderShop productItem = ProductItemResponseToOrderShop.builder()
                .id(1)
                .productCode("PI1")
                .price(100.0)
                .qtyInStock(1)
                .build();

        when(productItemService.getProductItemResponseById(eq(1)))
                .thenReturn(productItem);

        mockMvc.perform(get("/productItems/byId/{productItemId}", productItem.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productCode").value("PI1"));
    }

    @Test
    void ProductItemController_GetProductItemResponseByIds_Success() throws Exception {

        ProductItemOneByColourResponse productItem1 = ProductItemOneByColourResponse.builder()
                .productItemId(1)
                .productId(1)
                .productName("Test1")
                .build();

        ProductItemOneByColourResponse productItem2 = ProductItemOneByColourResponse.builder()
                .productItemId(2)
                .productId(1)
                .productName("Test2")
                .build();

        when(productItemService.getProductItemByIds(any()))
                .thenReturn(List.of(productItem1, productItem2));

        mockMvc.perform(get("/productItems/byIds")
                        .param("productItemIds", "1", "2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productItemId").value(1))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].productName").value("Test1"))
                .andExpect(jsonPath("$[1].productItemId").value(2))
                .andExpect(jsonPath("$[1].productId").value(1))
                .andExpect(jsonPath("$[1].productName").value("Test2"));
    }

    @Test
    void ProductItemController_UpdateStock_Success() throws Exception {

        ProductStockUpdateRequest request = ProductStockUpdateRequest.builder()
                .productItemId(1)
                .quantityToSubtract(5)
                .build();

        doNothing().when(productItemService).updateStock(any());

        mockMvc.perform(put("/productItems/update-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(List.of(request)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());

        verify(productItemService, times(1)).updateStock(any());
    }
}
