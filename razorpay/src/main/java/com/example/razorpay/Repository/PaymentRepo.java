package com.example.razorpay.Repository;
import com.example.razorpay.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepo extends MongoRepository<Transaction,String>
{
    Transaction findByOrderId(String orderId);
}
