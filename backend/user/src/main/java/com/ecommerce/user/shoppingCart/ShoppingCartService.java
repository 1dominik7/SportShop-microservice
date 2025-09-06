package com.ecommerce.user.shoppingCart;

import com.ecommerce.user.clients.ProductCallerService;
import com.ecommerce.user.clients.dto.*;
import com.ecommerce.user.discountCode.DiscountCode;
import com.ecommerce.user.discountCode.DiscountCodeRepository;
import com.ecommerce.user.exceptions.APIException;
import com.ecommerce.user.exceptions.NotFoundException;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCarItemGetProdItemResponse;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCartItem;
import com.ecommerce.user.shoppingCart.shoppingCartItem.ShoppingCartItemResponse;
import com.ecommerce.user.user.User;
import com.ecommerce.user.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartService {

    private final UserRepository userRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final ProductCallerService productCallerService;

    @Transactional
    public ShoppingCartResponse addProductToCart(Integer productItemId, Integer quantity, String currentKeycloakId) {

        if (productItemId == null || quantity == null || quantity <= 0) {
            throw new APIException("Invalid product or quantity");
        }

        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        ProductItemOneByColourResponse productItemResponse = productCallerService.getProductItemById(productItemId, null);
        if (productItemResponse == null || productItemResponse.getProductItemOneByColour() == null || productItemResponse.getProductItemOneByColour().isEmpty()) {
            return ShoppingCartResponse.builder()
                    .shoppingCartItems(Collections.emptyList())
                    .build();
        }

        var productItemDTO = productItemResponse.getProductItemOneByColour()
                .stream()
                .filter(pi -> pi.getId().equals(productItemId))
                .findFirst()
                .orElseThrow(() -> new APIException("Product item not found for id: " + productItemId));
        int availableQty = productItemDTO.getQtyInStock();

        if (user.getShoppingCart() == null) {
            user.setShoppingCart(new ShoppingCart());
            user.getShoppingCart().setShoppingCartItems(new ArrayList<>());
        }

        ShoppingCart cart = user.getShoppingCart();

        ShoppingCartItem item = cart.getShoppingCartItems().stream()
                .filter(i -> i.getProductItemId().equals(productItemId))
                .findFirst()
                .orElse(null);


        if (item != null) {
            if (item.getQty() + quantity > availableQty) {
                throw new APIException("Cannot add more than " + availableQty + " of product " + productItemResponse.getProductName());
            }
            item.setQty(item.getQty() + quantity);
        } else {
            if (quantity > availableQty) {
                throw new APIException("Only " + availableQty + " units of product " + productItemResponse.getProductName() + " are in stock");
            }

            ShoppingCartItem newItem = ShoppingCartItem.builder()
                    .productItemId(productItemId)
                    .qty(quantity)
                    .build();

            cart.getShoppingCartItems().add(newItem);
        }

        userRepository.save(user);

        List<ShoppingCartItemResponse> itemResponses = cart.getShoppingCartItems().stream()
                .map(i -> ShoppingCartItemResponse.builder()
                        .productItemId(i.getProductItemId())
                        .qty(i.getQty())
                        .build())
                .toList();

        return ShoppingCartResponse.builder()
                .shoppingCartItems(itemResponses)
                .build();
    }

    public ShoppingCartGetProdItemResponse getUserCartByKeycloak(String currentKeycloakId) {
        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        ShoppingCart cart = user.getShoppingCart();

        if (cart == null) {
            ShoppingCartGetProdItemResponse emptyResponse = new ShoppingCartGetProdItemResponse();
            emptyResponse.setShoppingCartItems(new ArrayList<>());
            emptyResponse.setDiscountCodes(new HashSet<>());
            return emptyResponse;
        }

        List<Integer> productItemIds = cart.getShoppingCartItems().stream()
                .map(ShoppingCartItem::getProductItemId)
                .collect(Collectors.toList());


        List<ProductItemOneByColourResponse> productItems;
        if (productItemIds.isEmpty()) {
            productItems = Collections.emptyList();
        } else {
            productItems = productCallerService.getProductItemByIds(productItemIds);
        }

        Map<Integer, ProductItemOneByColourResponse> productMap = productItems.stream()
                .filter(p -> p.getProductItemId() != null)
                .collect(Collectors.toMap(
                        ProductItemOneByColourResponse::getProductItemId,
                        p -> p
                ));

        List<ShoppingCarItemGetProdItemResponse> mappedItems = cart.getShoppingCartItems().stream()
                .map(item -> {
                    ProductItemOneByColourResponse productItem = productMap.get(item.getProductItemId());

                    if (productItem == null) {
                        throw new NotFoundException("ProductItem", Optional.of(item.getProductItemId().toString()));
                    }

                    return ShoppingCarItemGetProdItemResponse.builder()
                            .productItem(productItem)
                            .qty(item.getQty())
                            .build();
                })
                .collect(Collectors.toList());

        return ShoppingCartGetProdItemResponse.builder()
                .shoppingCartItems(mappedItems)
                .discountCodes(cart.getDiscountCodes())
                .build();
    }

    private void removeExpiredDiscountCodes(ShoppingCart shoppingCart) {
        LocalDateTime now = LocalDateTime.now();

        shoppingCart.getDiscountCodes().removeIf(discountCode ->
                discountCode.getExpiryDate().isBefore(now)
        );
    }

    private ProductItemRequest mapToProductItemRequestGrouped(ProductItemOneByColour productItem) {
        String colour = productItem.getColour() != null ? productItem.getColour() : "Unknown";

        String size = productItem.getVariations().stream()
                .filter(v -> v.getName().equalsIgnoreCase("size"))
                .flatMap(v -> v.getOptions().stream())
                .map(VariationOptionResponse::getValue)
                .findFirst()
                .orElse("Unknown");

        List<ProductImageResponse> sortedImages = productItem.getProductImages().stream()
                .sorted(Comparator.comparing(ProductImageResponse::getId))
                .collect(Collectors.toList());

        return ProductItemRequest.builder()
                .id(productItem.getId())
                .price(productItem.getPrice())
                .discount(productItem.getDiscount())
                .productCode(productItem.getProductCode())
                .qtyInStock(productItem.getQtyInStock())
                .productName(productItem.getProductName())
                .productId(productItem.getProductId())
                .colour(colour)
                .size(size)
                .productImages(sortedImages)
                .variations(productItem.getVariations())
                .build();
    }

    @Transactional
    public ShoppingCartResponse updateProductQuantityInCart(Integer productItemId, Integer quantity, String currentKeycloakId) {
        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        ShoppingCart shoppingCart = user.getShoppingCart();
        if (shoppingCart == null) {
            throw new NotFoundException("Shopping cart", Optional.empty());
        }

        ProductItemOneByColourResponse productItemResponse = productCallerService.getProductItemById(productItemId, null);
        if (productItemResponse == null || productItemResponse.getProductItemOneByColour().isEmpty()) {
            throw new NotFoundException("Product", Optional.of(productItemId.toString()));
        }

        var productItem = productItemResponse.getProductItemOneByColour()
                .stream()
                .filter(pi -> pi.getId().equals(productItemId))
                .findFirst()
                .orElseThrow(() -> new APIException("Product item not found for id: " + productItemId));

        if (productItem.getQtyInStock() < quantity) {
            throw new APIException("Only " + productItem.getQtyInStock() + " units available for " + productItem.getProductName());
        }

        ShoppingCartItem shoppingCartItem = shoppingCart.getShoppingCartItems().stream()
                .filter(item -> item.getProductItemId().equals(productItemId))
                .findFirst()
                .orElse(null);

        if (shoppingCartItem == null) {
            if (quantity <= 0) {
                throw new APIException("Cannot remove product that is not in cart");
            }

            shoppingCartItem = ShoppingCartItem.builder()
                    .productItemId(productItemId)
                    .qty(quantity)
                    .build();
            shoppingCart.getShoppingCartItems().add(shoppingCartItem);
        } else {
            int newQty = shoppingCartItem.getQty() + quantity;
            if (newQty < 0) {
                throw new APIException("Quantity cannot be negative");
            } else if (newQty == 0) {
                shoppingCart.getShoppingCartItems().remove(shoppingCartItem);
            } else if (newQty > productItem.getQtyInStock()) {
                throw new APIException("Requested quantity exceeds stock");
            } else {
                shoppingCartItem.setQty(newQty);
            }
        }

        userRepository.save(user);

        List<ShoppingCartItemResponse> itemResponses = shoppingCart.getShoppingCartItems().stream()
                .map(item -> ShoppingCartItemResponse.builder()
                        .productItemId(item.getProductItemId())
                        .qty(item.getQty())
                        .build()).collect(Collectors.toList());

        return ShoppingCartResponse.builder()
                .shoppingCartItems(itemResponses)
                .build();
    }

    private ShoppingCart createCart(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        if (user.getShoppingCart() != null) {
            return user.getShoppingCart();
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setShoppingCartItems(new ArrayList<>());
        shoppingCart.setDiscountCodes(new HashSet<>());

        user.setShoppingCart(shoppingCart);
        userRepository.save(user);
        return shoppingCart;
    }

    @Transactional
    public String deleteProductFromCart(Integer productItemId, String currentKeycloakId) {
        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        ShoppingCart shoppingCart = user.getShoppingCart();
        if (shoppingCart == null) {
            throw new NotFoundException("Cart", Optional.empty());
        }

        ShoppingCartItem shoppingCartItem = shoppingCart.getShoppingCartItems().stream()
                .filter(item -> item.getProductItemId().equals(productItemId))
                .findFirst()
                .orElse(null);

        if (shoppingCartItem == null) {
            throw new NotFoundException("Product", Optional.empty());
        }

        shoppingCart.getShoppingCartItems().remove(shoppingCartItem);
        userRepository.save(user);

        ProductItemOneByColourResponse productResp = productCallerService.getProductItemById(productItemId, null);
        var productItem = productResp.getProductItemOneByColour()
                .stream()
                .filter(pi -> pi.getId().equals(productItemId))
                .findFirst()
                .orElseThrow(() -> new APIException("Product item not found for id: " + productItemId));
        String productName = productItem.getProductName();
        String productCode = productItem.getProductCode();

        return "Product " + productName + "/" + productCode + " removed from the cart!";
    }

    public String addDiscountToCart(String discountCode, String currentKeycloakId) {
        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        ShoppingCart shoppingCart = user.getShoppingCart();
        if (shoppingCart == null) {
            throw new NotFoundException("Cart", Optional.empty());
        }
        System.out.println(discountCode);
        DiscountCode discount = discountCodeRepository.findByCode(discountCode).orElseThrow(() -> new RuntimeException("Discount with this code does not exist!"));

        if (discount.getExpiryDate() != null && discount.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new APIException("Discount code has expired!");
        }

        boolean alreadyExists = shoppingCart.getDiscountCodes().stream()
                .anyMatch(existing -> existing.getId().equals(discount.getId()));

        if (alreadyExists) {
            throw new APIException("Discount code already applied to this cart");
        }

        shoppingCart.getDiscountCodes().add(discount);
        userRepository.save(user);
        return "Discount code " + discountCode + " has been added";
    }

    @Transactional
    public String deleteCart(String currentKeycloakId) {
        User user = userRepository.findByKeycloakId(currentKeycloakId)
                .orElseThrow(() -> new NotFoundException("User", Optional.empty()));

        ShoppingCart shoppingCart = user.getShoppingCart();
        if (shoppingCart == null) {
            throw new NotFoundException("Cart", Optional.empty());
        }

        shoppingCart.getShoppingCartItems().clear();
        shoppingCart.getDiscountCodes().clear();

        userRepository.save(user);

        return "Shopping cart removed successfully";
    }
}
