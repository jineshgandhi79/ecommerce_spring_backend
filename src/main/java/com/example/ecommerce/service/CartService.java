package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartItemRepository cartItemRepository, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    @Transactional
    public CartItem addToCart(AddToCartRequest request) {
        // Validate product exists
        Product product = productService.getProductById(request.getProductId());

        // Check stock availability
        if (product.getStock() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Check if item already exists in cart
        return cartItemRepository.findByUserIdAndProductId(request.getUserId(), request.getProductId())
                .map(existingItem -> {
                    int newQuantity = existingItem.getQuantity() + request.getQuantity();

                    if (product.getStock() < newQuantity) {
                        throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
                    }

                    existingItem.setQuantity(newQuantity);
                    return cartItemRepository.save(existingItem);
                })
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUserId(request.getUserId());
                    newItem.setProductId(request.getProductId());
                    newItem.setQuantity(request.getQuantity());
                    return cartItemRepository.save(newItem);
                });
    }

    public List<CartItemResponse> getUserCart(String userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        return cartItems.stream().map(cartItem -> {
            CartItemResponse response = new CartItemResponse();
            response.setId(cartItem.getId());
            response.setProductId(cartItem.getProductId());
            response.setQuantity(cartItem.getQuantity());

            try {
                Product product = productService.getProductById(cartItem.getProductId());
                response.setProduct(product);
            } catch (ResourceNotFoundException e) {
                response.setProduct(null);
            }

            return response;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, String> clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Cart cleared successfully");
        return response;
    }

    public List<CartItem> getCartItemsForUser(String userId) {
        return cartItemRepository.findByUserId(userId);
    }
}