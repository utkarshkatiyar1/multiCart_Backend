package com.multikart.wishlist.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "wishlist")
@Data
public class Wishlist {
    @Id
    private String id;
    private String userId;
    List<Wishlist_item> wishlistItems;

    @Data
    public static class Wishlist_item{
        private int variantid_qty;
        private String productId;
        private String variantId;
    }
}
