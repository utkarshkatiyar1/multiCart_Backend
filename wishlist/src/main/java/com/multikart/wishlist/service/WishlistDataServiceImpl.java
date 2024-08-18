package com.multikart.wishlist.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multikart.wishlist.Repository.WishlistRepo;
import com.multikart.wishlist.common.Constants;
import com.multikart.wishlist.model.ApplicationResponse;
import com.multikart.wishlist.model.Cart;
import com.multikart.wishlist.model.Product;
import com.multikart.wishlist.model.Wishlist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WishlistDataServiceImpl implements WishlistDataService {

    @Autowired
    WishlistRepo wishlistRepo;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public ApplicationResponse getWishlistByUserId(String userId) {
//        List<Wishlist> wishlists =  wishlistRepo.findByUserId(userId);
//        ApplicationResponse<Wishlist> applicationResponse = new ApplicationResponse<>();
//        applicationResponse.setData(wishlists);
//        applicationResponse.setStatus(Constants.OK);
//        applicationResponse.setMessage(Constants.OK_MESSAGE);
//        return applicationResponse;
        ApplicationResponse response = new ApplicationResponse();
        Wishlist existingWishlist = null;

        existingWishlist = wishlistRepo.findWishlistByUserId(userId);
        if(existingWishlist==null)
        {
            log.warn("wishlist is not found with userId {}", userId);
            response.setData(null);
            response.setStatus(Constants.NO_CONTENT);
            response.setMessage(Constants.NO_CONTENT_MESSAGE);
            return  response;
        }
        List<Product> products = new ArrayList<>();

        for (Wishlist.Wishlist_item wishlistItem : existingWishlist.getWishlistItems()) {
            try {
                int variantIDQty = wishlistItem.getVariantid_qty();
                String productId = wishlistItem.getProductId();
                String productApiUrl = "http://localhost:8085/multikart/v1/product/byvariantid";

                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(productApiUrl)
                        .queryParam("productId", productId)
                        .queryParam("variantId", wishlistItem.getVariantId());

                ApplicationResponse<Map<String, Object>> productResponse = restTemplate.exchange(
                        builder.toUriString(),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<ApplicationResponse<Map<String, Object>>>() {
                        }
                ).getBody();

                if (productResponse != null && productResponse.getStatus().equals(Constants.OK)) {
                    Map<String, Object> productMap = productResponse.getData().get(0);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Product product = objectMapper.convertValue(productMap, Product.class);
                    product.getVariants().get(0).setVariantid_qty(String.valueOf(variantIDQty));
                    product.getVariants().get(0).setVariant_stock_qty(10);
                    products.add(product);
                } else {
                    log.warn("Failed to retrieve product details for productId: {} and variantId: {}", productId, wishlistItem.getVariantId());
                }
            } catch (Exception e) {
                log.error("An error occurred while fetching product details", e);
            }
        }

        response.setStatus(Constants.OK);
        response.setMessage(Constants.OK_MESSAGE);
        response.setData(Collections.singletonList(products));

        return response;
    }

    @Override
    public ApplicationResponse addToWishlist(Wishlist wishlist) {
        try{
            Wishlist existingWishlist = wishlistRepo.findWishlistByUserId(wishlist.getUserId());

            if (existingWishlist == null) {
                existingWishlist = new Wishlist();
                existingWishlist.setUserId(wishlist.getUserId());
                existingWishlist.setWishlistItems(new ArrayList<>());
            }

            List<Wishlist.Wishlist_item> userItems = wishlist.getWishlistItems();

            ApplicationResponse applicationResponse = new ApplicationResponse();
            for (Wishlist.Wishlist_item userItem : userItems) {
                boolean itemExists = false;
                for (Wishlist.Wishlist_item item : existingWishlist.getWishlistItems()) {
                    if (item.getVariantId().equals(userItem.getVariantId()) && item.getProductId().equals(userItem.getProductId()))
                    {
                        item.setVariantid_qty(item.getVariantid_qty() + userItem.getVariantid_qty());
                        applicationResponse.setMessage("Product is already added to the wishlist!!!");
                        itemExists = true;
                        break;
                    }
                }

                if (!itemExists) {
                    Wishlist.Wishlist_item newItem = new Wishlist.Wishlist_item();
                    newItem.setVariantid_qty(userItem.getVariantid_qty());
                    newItem.setVariantId(userItem.getVariantId());
                    newItem.setProductId(userItem.getProductId());
                    existingWishlist.getWishlistItems().add(newItem);
                }

            }
            wishlistRepo.save(existingWishlist);

            applicationResponse.setStatus(Constants.OK);
            applicationResponse.setMessage(Constants.OK_MESSAGE);
            return applicationResponse;

        } catch (Exception e)
        {
            log.error("An error occurred while adding product to wishlist", e);
            ApplicationResponse errorResponse = new ApplicationResponse();
            errorResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            errorResponse.setMessage("An error occurred while adding product to wishlist: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public ApplicationResponse removeFromWishlist(String userId, String productId, String variantId) {
        try {
            Wishlist existingWishlist = wishlistRepo.findWishlistByUserId(userId);
            List<Wishlist.Wishlist_item> existUserItems = existingWishlist.getWishlistItems();
            ApplicationResponse applicationResponse = new ApplicationResponse();
            existUserItems.removeIf(wishlistItem -> {
                try {

                   return wishlistItem.getProductId().equals(productId) && wishlistItem.getVariantId().equals(variantId);


                } catch (NumberFormatException e) {
                    log.error("Error parsing variant_id to integer: {}", variantId, e);
                    return false;
                }
            });

                existingWishlist.setWishlistItems(existUserItems);
                wishlistRepo.save(existingWishlist);
                applicationResponse.setStatus(Constants.OK);
                applicationResponse.setMessage(Constants.OK_MESSAGE);
                applicationResponse.setData(null);
                return applicationResponse;


        } catch (Exception e)
        {
            log.error("An error occurred while removing product from wishlist", e);
            ApplicationResponse errorResponse = new ApplicationResponse();
            errorResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            errorResponse.setMessage("An error occurred while removing product from wishlist: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public ApplicationResponse addAllToCart(String userId) {
        ApplicationResponse applicationResponse = new ApplicationResponse();
        try {
            Wishlist wishlist = wishlistRepo.findWishlistByUserId(userId);
            if (wishlist.getWishlistItems().isEmpty()) {
                log.error("Your wishlist is empty!...");
            }
            // Initialize a list to store cart items
            List<Cart.CartItem> cartItems = new ArrayList();

            for (Wishlist.Wishlist_item wishlistItem : wishlist.getWishlistItems()) {
                //add to cart for each wishlisted item
                //api for adding product to cart http://localhost:8081/multikart/v1/cart/add
                // Create a new cart item from the wishlist item
                Cart.CartItem cartItem = new Cart.CartItem();
                cartItem.setVariantid_qty(wishlistItem.getVariantid_qty());
                cartItem.setVariantid(Integer.parseInt(wishlistItem.getVariantId()));
                cartItem.setProductid(wishlistItem.getProductId());

                // Add the cart item to the list
                cartItems.add(cartItem);

                // Remove the product from the wishlist
               // wishlist.getWishlistItems().remove(wishlistItem);
            }

            // Create the request body
            Cart requestBody = new Cart();
            requestBody.setCartItems(cartItems);
            requestBody.setUserid(userId);

            // Call the API to add cart items
            String apiUrl = "http://localhost:8081/multikart/v1/cart/add";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Cart> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                // If the API call is successful, remove items from the wishlist
                wishlist.getWishlistItems().clear();
                wishlistRepo.save(wishlist);

                applicationResponse.setMessage("Products added to cart successfully!");
            } else {
                applicationResponse.setMessage("Failed to add products to cart. API call returned: " + responseEntity.getStatusCode());
            }
            return  applicationResponse;
        }catch (Exception e){
            log.error("An error occurred while adding all products to cart", e);
            ApplicationResponse errorResponse = new ApplicationResponse();
            errorResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            errorResponse.setMessage("An error occurred while adding all products to cart: " + e.getMessage());
            return errorResponse;
        }
    }
}
