package multiKart.product.Repository;
import multiKart.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;



    @Repository
    public interface ProductRepo extends MongoRepository<Product, String> {

        List<Product> findByCategoryIgnoreCase(String category);

        @Query("{$or:[{'title': { $regex: ?0, $options: 'i' }}," +
                " {'description': { $regex: ?0, $options: 'i' }}," +
                " {'brand': { $regex: ?0, $options: 'i' }}]}")
        List<Product> findByTitleContaining(String keyword);


    }





