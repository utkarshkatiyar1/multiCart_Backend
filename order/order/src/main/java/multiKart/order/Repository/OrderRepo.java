package multiKart.order.Repository;
import multiKart.order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepo extends MongoRepository <Order,String>
{
    List<Order> findByUserId(Long userId);
}
