package com.example.order_inventory_service.service;

import com.example.order_inventory_service.dto.CartRequest;
import com.example.order_inventory_service.model.Cart;
import com.example.order_inventory_service.repository.CartRepository;
import org.springframework.stereotype.Service;

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

    public Optional<Cart> getCart(String userId) {
        return cartRepository.findByUserId(userId);
    }

    public void deleteCart(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cartRepository::delete);
    }
}
