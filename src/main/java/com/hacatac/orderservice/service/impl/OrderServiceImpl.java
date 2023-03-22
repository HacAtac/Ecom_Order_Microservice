package com.hacatac.orderservice.service.impl;

import com.hacatac.orderservice.entity.Order;
import com.hacatac.orderservice.external.client.ProductService;
import com.hacatac.orderservice.model.request.OrderRequest;
import com.hacatac.orderservice.repository.OrderRepository;
import com.hacatac.orderservice.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductService productService;

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

        log.info("Order Placed Successfully with Order Id: {}", order.getId());

        return order.getId();
    }
}
