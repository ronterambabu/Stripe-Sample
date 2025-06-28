package com.zn.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String productName;
    private Long unitAmount; // in cents
    private Long quantity;
    private String currency;
    private String successUrl;
    private String cancelUrl;
    private String email;

    // Getters and Setters
}
