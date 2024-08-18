package com.multikart.cart.controller;
import com.multikart.cart.model.ApplicationResponse;
import com.multikart.cart.model.Cart;
import com.multikart.cart.service.CartDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/multikart/v1/cart")
public class CartController {
    @Autowired
    CartDataService cartDataService;

    @Operation(summary="remove product from cart ")
    @Tag(name= "cart remove")
    @DeleteMapping("/remove")
    public final ApplicationResponse removeFromCart(@RequestParam String variant_id ,@RequestParam String product_id, @RequestParam String user_id,@RequestParam String deleteType)
    {
       return cartDataService.removeFromCart(variant_id,user_id,deleteType,product_id);


    }

    @Operation(summary="add product to the cart ")
    @Tag(name= "add product to the cart")

    @PostMapping("/add")
    public ApplicationResponse addToCart(@RequestBody Cart cart)
    {
         return cartDataService.addToCart(cart);

    }

    @Operation(summary = "Get the cart items by it's user id")
    @GetMapping("/byuserid")
    public final ApplicationResponse getCartByVariantId(@RequestParam String userId)
    {
        return cartDataService.getCartByVariantId(userId);
    }

    @Operation(summary = "empty the cart for a user") //used in order MS
    @Tag(name = "empty cart")
    @DeleteMapping("/empty")
    public ApplicationResponse emptyCartForUser(@RequestParam String user_id)
    {
        return cartDataService.removeCartForUser(user_id);
    }

}
