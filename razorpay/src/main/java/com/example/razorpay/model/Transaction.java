package com.example.razorpay.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "Transaction")
public class Transaction {
    @Id
    private String orderId;
    private String transactionId;
    private int amount;
    private Date created_at;
    private String currency;
    private String receipt;
    private String offer_id;
    private String status;
    private String paymentId;

}
