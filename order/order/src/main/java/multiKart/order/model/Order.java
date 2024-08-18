package multiKart.order.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "order")
public class Order
{
    @Id
    private String id;
    private String modeOfPayment;
    private Long userId;
    private double totalAmount;
    private OrderStatus orderStatus = OrderStatus.Pending;
    private  String paymentStatus="pending";
    private BillingDetails billingDetails;
    private List<Product> products;
    private String createdAt;
    @Data
    public  static  class Product
    {
        private String product_id;
        private String variant_id;
        private int qty;
        private float price;
    }
    @Data
    public static class BillingDetails {
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String email;
        private String country;
        private String address;
        private String city;
        private String state;
        private String postalCode;

    }




}



