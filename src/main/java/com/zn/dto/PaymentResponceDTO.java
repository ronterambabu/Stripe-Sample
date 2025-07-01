package com.zn.dto;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class PaymentResponceDTO {
    private String id;
    private String url;
    private String payment_status;
    private String status;
    private Object metadata;
    private LocalDateTime created_at;
    private LocalDateTime expires_at;

    // Getters
    
}
