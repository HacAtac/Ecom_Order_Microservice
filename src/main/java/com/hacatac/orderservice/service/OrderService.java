package com.hacatac.orderservice.service;

import com.hacatac.orderservice.model.request.OrderRequest;
import com.hacatac.orderservice.model.response.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
