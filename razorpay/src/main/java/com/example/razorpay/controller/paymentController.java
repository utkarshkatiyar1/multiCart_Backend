package com.example.razorpay.controller;

import com.example.razorpay.Repository.PaymentRepo;
import com.example.razorpay.common.Constants;
import com.example.razorpay.model.ApplicationResponse;
import com.example.razorpay.model.Transaction;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/payment")
public class paymentController {

    @Autowired
    PaymentRepo paymentRepo;

    @Operation(summary = "get all transactions")
    @Tag(name = "List of all transactions")
    @GetMapping
    public ApplicationResponse getTransactions()
    {
        try {
            List<Transaction> transactions = paymentRepo.findAll();
            ApplicationResponse response = new ApplicationResponse();
            response.setData(transactions);
            response.setMessage("List of all Transactions");
            response.setStatus(Constants.OK);
            return response;
        }catch (Exception e) {
            log.error("An error occurred while getting all Transactions", e.getMessage());

            ApplicationResponse error = new ApplicationResponse();
            error.setStatus(Constants.INTERNAL_SERVER_ERROR);
            error.setMessage("An error occurred while getting all Transactions");
            return error;
        }
    }

    @Operation(summary = "create transaction")
    @Tag(name = "create transaction")
    @PostMapping("/createOrder")
    public ApplicationResponse createOrder(@RequestParam String orderId, @RequestParam int amount) throws Exception {
        var client = new RazorpayClient("rzp_test_qs6do2s9fX28HB","Ht0B5rxZuTtlhO7LwsMWLxJH");
        JSONObject ob = new JSONObject();
        ob.put("amount",amount*100);
        ob.put("currency","USD");
        ob.put("receipt","txn_235425");
        ApplicationResponse applicationResponse = new ApplicationResponse();

        try {
            //creating new order
            Order order = client.orders.create(ob);

            //save the order in database
            Transaction transaction = new Transaction();
            transaction.setAmount(order.get("amount"));
            transaction.setCurrency(order.get("currency"));
            transaction.setCreated_at(order.get("created_at"));
            transaction.setTransactionId(order.get("id"));
            transaction.setReceipt(order.get("receipt"));
            transaction.setOffer_id(order.get("offer_id").toString());
            transaction.setStatus(order.get("status"));
            transaction.setOrderId(orderId);

            paymentRepo.save(transaction);

            applicationResponse.setStatus(Constants.OK);
            applicationResponse.setMessage("Transaction details saved successfully!!!");
            applicationResponse.setData(Collections.singletonList(transaction));

        }catch (Exception e){
            applicationResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            applicationResponse.setMessage("Error in saving Transaction details");
        }
        return applicationResponse;
    }

    @Operation(summary = "update payment status")
    @Tag(name = "update payment status")
    @PostMapping("/updatePaymentStatus")
    public ApplicationResponse updatePaymentStatus(@RequestParam String paymentId, @RequestParam String orderId, @RequestParam String status) {
        ApplicationResponse applicationResponse = new ApplicationResponse();
        try {
            Transaction transaction = paymentRepo.findByOrderId(orderId);
            transaction.setPaymentId(paymentId);
            transaction.setStatus(status);
            paymentRepo.save(transaction);
            applicationResponse.setStatus(Constants.OK);
            applicationResponse.setMessage("Payment status updated successfully...");
            applicationResponse.setData(Collections.singletonList(transaction));

        }catch (Exception e) {
            applicationResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            applicationResponse.setMessage("payment status updation failed on server side...");
        }
        return applicationResponse;

    }
}
