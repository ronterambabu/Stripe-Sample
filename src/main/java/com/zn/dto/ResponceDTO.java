package com.zn.dto;

public class ResponceDTO {
    private String id;
    private String url;
    private String payment_status;
    private String status;
    private Object metadata;
    private String created;
    private String expires_at;

    public ResponceDTO() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(String expires_at) {
        this.expires_at = expires_at;
    }
}
