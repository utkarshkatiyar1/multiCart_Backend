package com.multikart.wishlist.service;


import com.multikart.wishlist.model.ApplicationResponse;
import com.multikart.wishlist.model.Wishlist;

import java.util.List;

public interface WishlistDataService
{

    ApplicationResponse getWishlistByUserId(String userId);

    ApplicationResponse addToWishlist(Wishlist wishlist);

    ApplicationResponse removeFromWishlist(String userId, String productId, String variantId);

    ApplicationResponse addAllToCart(String userId);
}
