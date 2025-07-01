package com.zn.dto;

import java.time.LocalDateTime;

public class ResponceDTO {
    private String id;
    private String url;
    private String payment_status;
    private String status;
    private Object metadata;
    private LocalDateTime created_at;
    private LocalDateTime expires_at;

    // Getters
    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public String getStatus() {
        return status;
    }

    public Object getMetadata() {
        return metadata;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public LocalDateTime getExpires_at() {
        return expires_at;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public void setExpires_at(LocalDateTime expires_at) {
        this.expires_at = expires_at;
    }
}
