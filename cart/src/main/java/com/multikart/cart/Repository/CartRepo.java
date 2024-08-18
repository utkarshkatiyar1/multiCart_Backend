package com.multikart.cart.Repository;
import com.multikart.cart.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepo extends MongoRepository<Cart,String>
{
   Cart findCartByUserid(String userid);

   void deleteByUserid(String userid);
}
