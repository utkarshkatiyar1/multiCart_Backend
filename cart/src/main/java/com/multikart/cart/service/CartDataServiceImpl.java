package com.multikart.cart.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multikart.cart.Repository.CartRepo;
import com.multikart.cart.common.Constants;
import com.multikart.cart.model.ApplicationResponse;
import com.multikart.cart.model.Cart;
import com.multikart.cart.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Slf4j
@Service
public class CartDataServiceImpl implements CartDataService {

    @Autowired
    CartRepo cartRepo;
    @Value("${product.microservice.base-url}")
    private String productMicroserviceBaseUrl;
    @Autowired
    private RestTemplate restTemplate;


    @Override
    public ApplicationResponse removeFromCart(String variant_id, String user_id, String deleteType, String product_id) {
        try {
            Cart existingCart = cartRepo.findCartByUserid(user_id);
            List<Cart.CartItem> existUserItems = existingCart.getCartItems();

            if ("remove_all".equals(deleteType)) {
                removeItemFromCart(existUserItems, variant_id);
            } else if ("increase_1".equals(deleteType)) {
                increaseQuantity(existUserItems, variant_id);
            } else if ("decrease_1".equals(deleteType)) {
                decreaseQuantity(existUserItems, variant_id);
            } else {
                log.error("Invalid deleteType: {}", deleteType);
            }

            existingCart.setCartItems(existUserItems);
            cartRepo.save(existingCart);
            ApplicationResponse applicationResponse = new ApplicationResponse();
            applicationResponse.setStatus(Constants.OK);
            applicationResponse.setMessage(Constants.OK_MESSAGE);
            return applicationResponse;

        } catch (Exception e)
        {
            log.error("An error occurred while removing variant from cart", e);
            ApplicationResponse errorResponse = new ApplicationResponse();
            errorResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            errorResponse.setMessage("An error occurred while removing variant from cart: " + e.getMessage());
            return errorResponse;
        }
    }

    private void removeItemFromCart(List<Cart.CartItem> existUserItems, String variant_id) {
        existUserItems.removeIf(cartItem -> {
            try {
                return cartItem.getVariantid() == Integer.parseInt(variant_id);
            } catch (Exception e) {
                log.error("Error while decreasing quantity with variant_id {}", variant_id, e);
                return false;
            }
        });
    }

    private void increaseQuantity(List<Cart.CartItem> existUserItems, String variant_id) {
        for (Cart.CartItem cartItem : existUserItems) {
            try {
                if (cartItem.getVariantid() == Integer.parseInt(variant_id)) {
                    cartItem.setVariantid_qty(cartItem.getVariantid_qty() + 1);
                    break;
                }
            } catch (Exception e) {
                log.error("Error while decreasing quantity with variant_id {}", variant_id, e);
            }
        }
    }

    private void decreaseQuantity(List<Cart.CartItem> existUserItems, String variant_id)
    {
        existUserItems.removeIf(cartItem -> {
            try {
                if (cartItem.getVariantid() == Integer.parseInt(variant_id)) {
                    if (cartItem.getVariantid_qty() > 1) {
                        cartItem.setVariantid_qty(cartItem.getVariantid_qty() - 1);
                        return false;
                    } else {
                        return true;
                    }
                }
            } catch (Exception e) {
                log.error("Error while decreasing quantity with variant_id {}", variant_id, e);
            }
            return false;
        });
    }


    @Override
    public ApplicationResponse addToCart(Cart cart) {
        try {
            Cart existingCart = cartRepo.findCartByUserid(cart.getUserid());

            if (existingCart == null) {
                existingCart = new Cart();
                existingCart.setUserid(cart.getUserid());
                existingCart.setCartItems(new ArrayList<>());
            }
            List<Cart.CartItem> userItems = cart.getCartItems();
            boolean itemExists = false;

            for (Cart.CartItem userItem : userItems) {
                for (Cart.CartItem item : existingCart.getCartItems()) {
                    if (item.getVariantid() == userItem.getVariantid() && item.getProductid().equals(userItem.getProductid()))
                    {
                        item.setVariantid_qty(item.getVariantid_qty() + userItem.getVariantid_qty());
                        itemExists = true;
                        break;
                    }
                }

                if (!itemExists) {
                    Cart.CartItem newItem = new Cart.CartItem();
                    newItem.setVariantid_qty(userItem.getVariantid_qty());
                    newItem.setVariantid(userItem.getVariantid());
                    newItem.setProductid(userItem.getProductid());
                    existingCart.getCartItems().add(newItem);
                }

            }
            cartRepo.save(existingCart);

            ApplicationResponse applicationResponse = new ApplicationResponse();
            applicationResponse.setStatus(Constants.OK);
            applicationResponse.setMessage(Constants.OK_MESSAGE);
            return applicationResponse;

        } catch (Exception e) {
            log.error("An error occurred while adding product to cart", e.getMessage());

            ApplicationResponse errorAddingProductToCart = new ApplicationResponse();
            errorAddingProductToCart.setStatus(Constants.INTERNAL_SERVER_ERROR);
            errorAddingProductToCart.setMessage("An error occurred while adding product to cart");
            return errorAddingProductToCart;
        }
    }

    @Override
    public ApplicationResponse removeCartForUser(String user_id)   //created for order microService after  order , cart will be deleted once order done
    {

            try {
                cartRepo.deleteByUserid(user_id);
                ApplicationResponse applicationResponse = new ApplicationResponse();
                applicationResponse.setStatus(Constants.OK);
                applicationResponse.setMessage(Constants.OK_MESSAGE);
                return applicationResponse;

            } catch (Exception e) {
                log.error("An error occurred while emptying the cart for user", e);
                ApplicationResponse errorResponse = new ApplicationResponse();
                errorResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
                errorResponse.setMessage("An error occurred while emptying the cart for user: " + e.getMessage());
                return errorResponse;
            }
        }



    @Override
    public ApplicationResponse getCartByVariantId(String userId) {
        ApplicationResponse response = new ApplicationResponse();
        Cart existingCart = null;

            existingCart = cartRepo.findCartByUserid(userId);
            if(existingCart==null)
            {
            log.warn("cart is not found with userId {}", userId);
            response.setData(null);
            response.setStatus(Constants.NO_CONTENT);
            response.setMessage(Constants.NO_CONTENT_MESSAGE);
            return  response;
           }
        List<Product> products = new ArrayList<>();

        for (Cart.CartItem cartItem : existingCart.getCartItems()) {
            try {
                int variantIDQty = cartItem.getVariantid_qty();
                String productId = cartItem.getProductid();
                String productApiUrl = "http://localhost:8085/multikart/v1/product/byvariantid";

                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(productApiUrl)
                        .queryParam("productId", productId)
                        .queryParam("variantId", cartItem.getVariantid());

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
                    log.warn("Failed to retrieve product details for productId: {} and variantId: {}", productId, cartItem.getVariantid());
                }
            } catch (Exception e) {
                log.error("An error occurred while fetching product details", e);
            }
        }

        response.setStatus(Constants.OK);
        response.setMessage(Constants.OK_MESSAGE);
        response.setData(Collections.singletonList(products));

        return response;
    }}





