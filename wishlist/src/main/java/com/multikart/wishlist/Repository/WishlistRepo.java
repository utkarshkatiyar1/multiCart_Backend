package com.multikart.wishlist.Repository;
import com.multikart.wishlist.model.ApplicationResponse;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.multikart.wishlist.model.Wishlist;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface WishlistRepo extends  MongoRepository<Wishlist,String> {
    List<Wishlist> findByUserId(String userId);

    Wishlist findWishlistByUserId(String userId);
}
