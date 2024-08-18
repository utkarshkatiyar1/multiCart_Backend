package multiKart.order.service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import multiKart.order.Repository.OrderRepo;
import multiKart.order.common.Constants;
import multiKart.order.model.ApplicationResponse;
import multiKart.order.model.Order;
import multiKart.order.model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.util.*;




@Slf4j
@Service
public class OrderDataSvcImpl implements OrderDataService {
    @Value("${cart.microservice.base-url}")
    private String cartMicroserviceBaseUrl;
    @Value("${product.microservice.base-url}")
    private String productMicroserviceBaseUrl;
    @Autowired
    OrderRepo orderRepo;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${spring.mail.username}")
    private String emailSender;


    @Value("${spring.mail.subject}")
    private String emailSubject;



    @Override
    @Transactional
    public ApplicationResponse createOrder(Order order)
    {
        Order savedOrder ;
        ApplicationResponse applicationResponse = new ApplicationResponse();
        String userEmail = order.getBillingDetails().getEmail();

        try {
            if (isOrderValid(order)) {  // check on request body

                if (isVariantInStock(order.getProducts())) {  //check whether that variant is in stock or not now
                    savedOrder = orderRepo.save(order);
                    //savedOrder.setOrderStatus("confirm");
                    savedOrder.setCreatedAt(LocalDateTime.now().toString());
                    savedOrder.setPaymentStatus("pending");
                    orderRepo.save(savedOrder);
                    applicationResponse.setStatus(Constants.OK);
                    applicationResponse.setMessage("Order placed successfully , your order id is- "+ savedOrder.getId());
                    emptyCartForUser(String.valueOf(order.getUserId()));
                    decreaseVariantStock(order.getProducts());
                    sendOrderConfirmationEmail(userEmail,order);
                } else
                {
                    applicationResponse.setStatus(Constants.BAD_REQUEST);
                    log.warn("Invalid order: Variant IDs not available , we hit the product micro service to know variant_id's availability ");
                    applicationResponse.setMessage("out of stock");

                }
            } else
            {
                applicationResponse.setStatus(Constants.BAD_REQUEST);
                log.warn("Invalid order: Quantity should be greater than 0 for at least one product");
                applicationResponse.setMessage("out of stock");

            }


        } catch (Exception e) {
            log.error("An error occurred while creating order", e);
            applicationResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
            applicationResponse.setMessage("An error occurred while placing order " + e.getMessage());

        }

        return applicationResponse;
    }


    @Override
    public ApplicationResponse getOrderAll(String userId) {
        ApplicationResponse applicationResponse = new ApplicationResponse();
        try {

            List<Order> orders = orderRepo.findByUserId(Long.parseLong(userId));
            if (orders != null && !orders.isEmpty()) {
                log.info("Orders retrieved successfully for userId " + userId);
                applicationResponse.setStatus(Constants.OK);
                applicationResponse.setMessage("Orders retrieved successfully for user name " + userId);
                applicationResponse.setData(orders);

            } else {

                applicationResponse.setStatus(Constants.NO_CONTENT);
                applicationResponse.setMessage("No orders found for the specified user");
                log.info("No orders found for the specified user and is id " + userId);
            }
        } catch (Exception e) {
            log.error("An error occurred while fetching orders for userId: {}", userId, e);
            return applicationResponse;
        }
        return applicationResponse;
    }

    @Override
    public void sendOrderConfirmationEmail(String to, Order order) throws MessagingException {
        Context context = new Context();
        List<Map<String, Object>> productDetailsList = buildProductDetailsList(order.getProducts());

        context.setVariable("productDetailsList", productDetailsList);
        String htmlContent = templateEngine.process("order-confirmation-template", context);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject(emailSubject);
        helper.setFrom(emailSender);
        helper.setTo(to);
        helper.setText(htmlContent, true);
        try {
            javaMailSender.send(message);
        }
        catch (Exception e)
        {  log.warn("exception occurred while sending mail to mail_id {} ", to);
        }
    }

    @Override
    public ApplicationResponse updateOrderStatus(String orderId, OrderStatus orderStatus) {
        ApplicationResponse applicationResponse = new ApplicationResponse<>();
        try {
            Order order = orderRepo.findById(orderId).orElse(null);
            if (order != null) {
                order.setOrderStatus(orderStatus);
            }
            else{
                log.error("order not found...");
            }
            orderRepo.save(order);

            applicationResponse.setStatus(Constants.OK);
            applicationResponse.setData(Collections.singletonList(order));
            applicationResponse.setMessage("order status updated successfully!!!");
        }catch (Exception e){
            applicationResponse.setMessage("order status not updated successfully!!!");
            applicationResponse.setStatus(Constants.INTERNAL_SERVER_ERROR);
        }
        return applicationResponse;
    }

    private List<Map<String, Object>> buildProductDetailsList(List<Order.Product> products) {
        List<Map<String, Object>> productDetailsList = new ArrayList<>();

        for (Order.Product product : products) {
            String productDetailsUrl = productMicroserviceBaseUrl + "byvariantid";
            String product_id = product.getProduct_id();
            String variant_id = product.getVariant_id();

            try {
                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(productDetailsUrl)
                        .queryParam("productId", product_id)
                        .queryParam("variantId", variant_id);

                ApplicationResponse<Map<String, Object>> applicationResponse = restTemplate.exchange(
                        builder.toUriString(),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<ApplicationResponse<Map<String, Object>>>() {
                        }
                ).getBody();

                if (applicationResponse != null && applicationResponse.getStatus() == 200) {
                    List<Map<String, Object>> productList = applicationResponse.getData();
                    for (Map<String, Object> list : productList) {
                        Map<String, Object> productDetails = buildProductDetails(product, list);
                        productDetailsList.add(productDetails);
                    }
                } else {
                    log.error("Exception occurred while creating e-mail message");
                }
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage());
            }
        }

        return productDetailsList;
    }

    private Map<String, Object> buildProductDetails(Order.Product product, Map<String, Object> productDetailsMap) {
//        String variant_id = String.valueOf(productDetailsMap.get("variant_id"));
//
//        if (!variant_id.equals(product.getVariant_id())) {
//            log.warn("Variant ID not found: {}", variant_id);
//            return Collections.emptyMap();
//        }

        String size = (String) productDetailsMap.get("size");
        String color = (String) productDetailsMap.get("color");
        String title = (String) productDetailsMap.get("title");
        double price = (double) productDetailsMap.get("price");

        Map<String, Object> productDetails = new HashMap<>();
        productDetails.put("title", title);
        productDetails.put("price", price);
        productDetails.put("color", color);
        productDetails.put("size", size);

        return productDetails;
    }




    private boolean isOrderValid(Order order) {
        if (order == null) {
            log.error("Invalid order: Order is null");
            return false;
        }

        List<Order.Product> products = order.getProducts();

        if (products == null || products.isEmpty()) {
            log.error("Invalid order: No products in the order");
            return false;
        }

        if (products.stream().anyMatch(product -> product.getQty() <= 0)) {
            log.error("Invalid order: Quantity should be greater than 0 for all products");
            return false;
        }

        return true;
    }

    private boolean isVariantInStock(List<Order.Product> products) {
        for (Order.Product product : products) {
            try {
                ResponseEntity<Boolean> responseEntity = restTemplate.getForEntity(
                        productMicroserviceBaseUrl + "/isVariantAvailable" +
                                "?productId={productId}&variantId={variantId}&qty={qty}",
                        Boolean.class,
                        product.getProduct_id(),
                        product.getVariant_id(),
                        product.getQty()
                );

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    if (responseEntity.getBody() != null && responseEntity.getBody())
                    {
                        continue;
                    }
                    else
                    {
                        log.error("Variant not available for productId: {}, variantId: {}",
                                product.getProduct_id(), product.getVariant_id());
                    }
                } else {
                    log.error("Error checking variant availability. Status code: {}", responseEntity.getStatusCode());
                }
                return false;
            } catch (Exception e) {
                log.error("An error occurred while checking variant availability", e);
                return false;
            }
        }

        return true; // All variants are in stock.
    }


    private void emptyCartForUser(String userId)
    {

        try
        {
            restTemplate.delete(cartMicroserviceBaseUrl + "empty?user_id={userId}", userId);//http://localhost:8081/multikart/v1/cart/empty?user_id=112
        }
        catch (HttpClientErrorException e)
        {
            log.error("We are calling cart microservice with url {} , Exception found while deleting cart for user- {}",cartMicroserviceBaseUrl, e.getMessage());

        }

    }


    private void decreaseVariantStock(List<Order.Product> products) {
        for (Order.Product product : products) {
            try {
                String url = productMicroserviceBaseUrl + "/updateVariantqty";
                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                        .queryParam("productId", product.getProduct_id())
                        .queryParam("variantId", product.getVariant_id())
                        .queryParam("requestQty", product.getQty());

                restTemplate.put(builder.toUriString(), null);

                log.info("Variant updated successfully for productId: {}, variantId: {}, requestQty: {}",
                        product.getProduct_id(), product.getVariant_id(), product.getQty());
            } catch (HttpClientErrorException e) {
                log.error("Error updating variant for productId: {}, variantId: {}. Error: {}",
                        product.getProduct_id(), product.getVariant_id(), e.getMessage());
            } catch (Exception e) {
                log.error("An error occurred while updating variant for productId: {}, variantId: {}", product.getProduct_id(), product.getVariant_id(), e);
            }
        }
    }
}
