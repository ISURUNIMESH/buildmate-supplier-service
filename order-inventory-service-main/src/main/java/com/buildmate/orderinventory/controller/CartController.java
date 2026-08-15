package com.buildmate.orderinventory.controller;

import com.buildmate.orderinventory.config.OpenApiConfig;
import com.buildmate.orderinventory.dto.CartRequest;
import com.buildmate.orderinventory.exception.ApiErrorResponse;
import com.buildmate.orderinventory.model.Cart;
import com.buildmate.orderinventory.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart", description = "Shopping cart operations. IDs are MongoDB/backend IDs, not friendly display IDs.")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add item to cart", description = "Creates a cart if none exists for the user, then appends the item.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item added",
                    content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public Cart addToCart(@Valid @RequestBody CartRequest request) { return cartService.addToCart(request); }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get cart by user ID",
            description = "Returns the persisted cart, or an unsaved empty cart with items:[] when none exists "
                    + "(getCartOrEmpty). Does NOT return 404 for a missing cart. "
                    + "userId is a MongoDB/backend ID, not a friendly display ID such as U_001."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart found, or empty cart with items:[] when none exists",
                    content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public Cart getCart(
            @Parameter(description = "MongoDB/backend user ID (not a friendly display ID like U_001)", required = true)
            @PathVariable String userId) {
        return cartService.getCartOrEmpty(userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete cart", description = "Deletes the persisted cart for the user. Returns 404 if no cart exists.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cart deleted"),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    public void deleteCart(
            @Parameter(description = "MongoDB/backend user ID", required = true) @PathVariable String userId) {
        cartService.deleteCart(userId);
    }
}
