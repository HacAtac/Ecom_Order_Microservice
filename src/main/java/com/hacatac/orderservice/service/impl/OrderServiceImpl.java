package com.hacatac.orderservice.service.impl;

import com.hacatac.orderservice.entity.Order;
import com.hacatac.orderservice.exception.CustomException;
import com.hacatac.orderservice.external.client.PaymentService;
import com.hacatac.orderservice.external.client.ProductService;
import com.hacatac.orderservice.model.request.OrderRequest;
import com.hacatac.orderservice.model.request.PaymentRequest;
import com.hacatac.orderservice.model.response.OrderResponse;
import com.hacatac.orderservice.model.response.ProductDetails;
import com.hacatac.orderservice.repository.OrderRepository;
import com.hacatac.orderservice.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        //Order Entity -> Save the data with Status Order Created
        //Product Service - Block Products (Reduce the Quantity)
        //Payment Service -> Payments -> Success -> COMPLETE, ELse -> CANCELLED

        log.info("Placing Order Request: {}", orderRequest);

        //call product microservice to reduce the quantity
        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Product Quantity Reduced Successfully for Product Id: {}", orderRequest.getProductId());

        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        order = orderRepository.save(order);

        log.info("Calling Payment Service to make Payment for Order Id: {}", order.getId());

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment Successful for Order Id: {}, Changing the Order status to PLACED: {}", order.getId());
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occurred while making payment for Order Id: {}, Changing the Order status to PAYMENT_FAILED: {}", order.getId());
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order Placed Successfully with Order Id: {}", order.getId());

        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Getting Order Details for Order Id: {}", orderId);
        Order order
                = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for the order Id" + orderId,
                        "NOT_FOUND", 404));

        log.info("Invoking Product service With RestTemplate to fetch the product details for Product Id: {}", order.getProductId());
        ProductDetails productDetails
                = restTemplate.getForObject(
                        "http://PRODUCT-SERVICE/product/" + order.getProductId(),
                ProductDetails.class
        );

        ProductDetails productDetail =
                ProductDetails.builder()
                        .productName(productDetails.getProductName())
                        .productId(productDetails.getProductId())
                        .price(productDetails.getPrice())
                        .quantity(order.getQuantity())
                        .build();

        OrderResponse orderResponse
                = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(productDetail)
                .build();

        return orderResponse;
    }
}
