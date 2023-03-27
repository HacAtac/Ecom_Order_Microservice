package com.hacatac.orderservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetails {
    private long productId;
    private String productName;
    private long price;
    private long quantity;
}
