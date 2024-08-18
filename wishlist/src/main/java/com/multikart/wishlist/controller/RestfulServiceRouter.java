package com.multikart.wishlist.controller;

import com.multikart.wishlist.model.ApplicationResponse;
import com.multikart.wishlist.model.Wishlist;
import com.multikart.wishlist.service.WishlistDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/multikart/product/wishlist")
public class RestfulServiceRouter {
    @Autowired
    private WishlistDataService wishlistDataService;

    @Operation(summary="get wishlist of user")
    @Tag(name= "get the wishlist")
    @GetMapping
    public ApplicationResponse getWishlistByUserId(@RequestParam String userId) {
        return wishlistDataService.getWishlistByUserId(userId);
    }

    @Operation(summary="add product to the wishlist")
    @Tag(name= "add product to the wishlist")
    @PostMapping("/add")
    public ApplicationResponse addToWishlist(@RequestBody Wishlist wishlist) {
        return wishlistDataService.addToWishlist(wishlist);
    }

    @Operation(summary="delete product from wishlist")
    @Tag(name= "delete product from wishlist")
    @DeleteMapping("/remove")
    public ApplicationResponse removeFromWishlist(@RequestParam String userId, @RequestParam String productId, @RequestParam String variantId) {
        return wishlistDataService.removeFromWishlist(userId, productId, variantId);
    }

    @Operation(summary="add all products from wishlist to cart")
    @Tag(name= "add all products from wishlist to cart")
    @PostMapping("/addAll")
    public ApplicationResponse addAllToCart(@RequestParam String userId) {
        return wishlistDataService.addAllToCart(userId);
    }

}
