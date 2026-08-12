package com.example.order_inventory_service.controller;

import com.example.order_inventory_service.dto.CartRequest;
import com.example.order_inventory_service.model.Cart;
import com.example.order_inventory_service.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cart addToCart(@Valid @RequestBody CartRequest request) {
        return cartService.addToCart(request);
    }

    @GetMapping("/{userId}")
    public Cart getCart(@PathVariable String userId) {
        return cartService.getCart(userId).orElseThrow(() -> new IllegalArgumentException("Cart not found"));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCart(@PathVariable String userId) {
        cartService.deleteCart(userId);
    }
}
