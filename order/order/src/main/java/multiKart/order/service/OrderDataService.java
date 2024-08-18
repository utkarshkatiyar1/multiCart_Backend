package multiKart.order.service;
import jakarta.mail.MessagingException;
import multiKart.order.model.ApplicationResponse;
import multiKart.order.model.Order;
import multiKart.order.model.OrderStatus;


public interface OrderDataService {

    ApplicationResponse createOrder(Order order);

    ApplicationResponse getOrderAll(String userId) ;

    void sendOrderConfirmationEmail(String to, Order order) throws MessagingException;

    ApplicationResponse updateOrderStatus(String orderId, OrderStatus orderStatus);
}
