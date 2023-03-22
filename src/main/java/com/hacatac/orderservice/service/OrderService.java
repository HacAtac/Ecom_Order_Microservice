package com.hacatac.orderservice.service;

import com.hacatac.orderservice.model.request.OrderRequest;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);
}
