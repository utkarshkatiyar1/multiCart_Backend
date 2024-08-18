package multiKart.rating.Repository;

import multiKart.rating.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepo extends MongoRepository <Rating,String>
{

    List<Rating> findByUserId(String userId);
    List<Rating>findByProductIdAndVariantId(String productId, String variantId);
}
