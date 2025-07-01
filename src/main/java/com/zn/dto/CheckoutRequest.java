package com.zn.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private String productName;
    private String description;
    private String orderReference;
    private Long unitAmount; // in cents
    private Long quantity;
    private String currency;
    private String successUrl;
    private String cancelUrl;
    private String customerEmail;

    // Explicit getters
    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public String getOrderReference() {
        return orderReference;
    }

    public Long getUnitAmount() {
        return unitAmount;
    }

    public Long getQuantity() {
        return quantity;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }
}
