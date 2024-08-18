package multiKart.order.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import multiKart.order.model.ApplicationResponse;
import multiKart.order.model.Order;
import multiKart.order.model.OrderStatus;
import multiKart.order.service.OrderDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/multikart/v1/order")
public class OrderController {

    @Autowired
    OrderDataService orderDataService;
    @Operation( summary = "create orders" )
    @Tag(name = "order")
    @PostMapping("/create" )
    public final ApplicationResponse createOrder (@RequestBody Order orders)
    {
        return orderDataService.createOrder(orders);

    }

    @Operation(summary="get all order ")
    @Tag(name="show all orders")
    @GetMapping("/order")
    public final ApplicationResponse getAllOrder(@RequestParam String userId)  {
        return  orderDataService.getOrderAll(userId);
    }

    @Operation(summary="update order status")
    @Tag(name="update order status")
    @PutMapping("/updateOrderStatus")
    public final ApplicationResponse updateOrderStatus(@RequestParam String orderId, @RequestParam OrderStatus orderStatus)  {
        return  orderDataService.updateOrderStatus(orderId, orderStatus);
    }

}
