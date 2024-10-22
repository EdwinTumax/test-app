package com.tigo.workersupermarketott.core.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderCreate implements Serializable {
    private String msisdn;
    private String productId;
    private String type;
    private String category;
    private String token;
    private String orderId;
}