package com.multikart.cart.service;
import com.multikart.cart.model.ApplicationResponse;
import com.multikart.cart.model.Cart;

public interface CartDataService
{

    ApplicationResponse removeFromCart(String variant_id, String user_id, String deleteType, String product_id);


    ApplicationResponse addToCart(Cart cart);

    ApplicationResponse removeCartForUser(String userId);

    ApplicationResponse getCartByVariantId(String userId);
}
