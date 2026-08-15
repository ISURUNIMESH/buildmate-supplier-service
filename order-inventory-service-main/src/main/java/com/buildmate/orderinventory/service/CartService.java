package com.buildmate.orderinventory.service;

import com.buildmate.orderinventory.dto.CartRequest;
import com.buildmate.orderinventory.exception.ResourceNotFoundException;
import com.buildmate.orderinventory.model.Cart;
import com.buildmate.orderinventory.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart addToCart(CartRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUserId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(request.getUserId());
            return newCart;
        });
        Cart.CartItem item = new Cart.CartItem();
        item.setMaterialId(request.getMaterialId());
        item.setQuantity(request.getQuantity());
        item.setPrice(request.getPrice());
        cart.getItems().add(item);
        return cartRepository.save(cart);
    }

    public Optional<Cart> getCart(String userId) { return cartRepository.findByUserId(userId); }

    /**
     * Returns the persisted cart, or an unsaved empty cart for the user when none exists.
     * Does not create a Mongo document until the first add-to-cart.
     */
    public Cart getCartOrEmpty(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart empty = new Cart();
            empty.setUserId(userId);
            empty.setItems(new ArrayList<>());
            return empty;
        });
    }

    public void deleteCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cartRepository.delete(cart);
    }
}
