package com.multikart.cart.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "cart")
@Data
public class Cart
{
    @Id
    private String cartid;
    private List<CartItem> cartItems;
    public String userid;




    @Data
    public static class CartItem {
        private int variantid_qty;
        private int variantid;
        private String productid;
    }

}