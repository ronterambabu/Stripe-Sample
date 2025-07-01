package com.zn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.zn.dto.CheckoutRequest;
import com.zn.dto.ResponceDTO;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {
    private final Logger logger = LoggerFactory.getLogger(StripeService.class);
    private final DateTimeFormatter US_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a z");
    private final ZoneId US_ZONE = ZoneId.of("America/New_York");

    @Value("${stripe.api.secret.key}")
    private String secretKey;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private String formatToUsTime(Long timestamp) {
        if (timestamp == null) return null;
        return Instant.ofEpochSecond(timestamp)
                     .atZone(US_ZONE)
                     .format(US_FORMAT);
    }

    private ResponceDTO mapSessionToDTO(Session session) {
        ResponceDTO responseDTO = new ResponceDTO();
        responseDTO.setId(session.getId());
        responseDTO.setUrl(session.getUrl());
        responseDTO.setPayment_status(session.getPaymentStatus());
        responseDTO.setStatus(session.getStatus());
        responseDTO.setMetadata(session.getMetadata());
        
        // Convert timestamps to US format
        String createdTime = formatToUsTime(session.getCreated());
        String expiresTime = formatToUsTime(session.getExpiresAt());
        
        responseDTO.setCreated(createdTime);
        responseDTO.setExpires_at(expiresTime);
        
        return responseDTO;
    }

    public Session createDetailedCheckoutSession(CheckoutRequest request) throws StripeException {
        logger.info("Creating detailed checkout session for product: {}", request.getProductName());
        Stripe.apiKey = secretKey;

        // Create metadata for better tracking
        Map<String, String> metadata = new HashMap<>();
        metadata.put("productName", request.getProductName());
        if (request.getOrderReference() != null) {
            metadata.put("orderReference", request.getOrderReference());
        }

        // Calculate expiration time in US Eastern time
        ZonedDateTime expirationTime = ZonedDateTime.now(US_ZONE).plus(30, ChronoUnit.MINUTES);
        
        // Build the session parameters
        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(request.getSuccessUrl())
            .setCancelUrl(request.getCancelUrl())
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setExpiresAt(expirationTime.toEpochSecond())
            .putAllMetadata(metadata)
            .setCustomerEmail(request.getCustomerEmail())
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(request.getQuantity())
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(request.getCurrency() != null ? request.getCurrency().toLowerCase() : "usd")
                            .setUnitAmount(request.getUnitAmount())
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(request.getProductName())
                                    .setDescription(request.getDescription())
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();

        try {
            Session session = Session.create(params);
            logger.info("Created checkout session with ID: {} at {}", 
                       session.getId(), 
                       formatToUsTime(session.getCreated()));
            return session;
        } catch (StripeException e) {
            logger.error("Error creating checkout session: {}", e.getMessage());
            throw e;
        }
    }

//    public String createCheckoutSession(CheckoutRequest request) throws Exception {
//        Session session = createDetailedCheckoutSession(request);
//        return session.getUrl();
//    }

    public Event constructWebhookEvent(String payload, String sigHeader) throws SignatureVerificationException {
        logger.debug("Constructing webhook event from payload with signature");
        return Webhook.constructEvent(payload, sigHeader, endpointSecret);
    }

    public void processWebhookEvent(Event event) {
        logger.info("Processing webhook event of type: {}", event.getType());
        
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            // Add more event type handlers here as needed
            default:
                logger.info("Unhandled event type: {}", event.getType());
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            Session session = (Session) dataObjectDeserializer.getObject().get();
            String completedTime = formatToUsTime(session.getCreated());
            
            logger.info("✅ Payment successful for session: {} at {}", session.getId(), completedTime);
            logger.info("💳 Customer email: {}", session.getCustomerDetails() != null ? 
                session.getCustomerDetails().getEmail() : "N/A");
            logger.info("💰 Amount total: {} at {}", 
                       session.getAmountTotal(),
                       completedTime);
            
            // TODO: Add your business logic here
            // For example:
            // - Update order status in database
            // - Send confirmation email
            // - Update inventory
            // - etc.
        } else {
            logger.warn("Event data object deserialization failed");
        }
    }

    public ResponceDTO retrieveSession(String sessionId) throws StripeException {
        logger.info("Retrieving session with ID: {}", sessionId);
        Stripe.apiKey = secretKey;
        
        try {
            Session session = Session.retrieve(sessionId);
            return mapSessionToDTO(session);
        } catch (StripeException e) {
            logger.error("Error retrieving session: {}", e.getMessage());
            throw e;
        }
    }

    public ResponceDTO expireSession(String sessionId) throws StripeException {
        logger.info("Expiring session with ID: {}", sessionId);
        Stripe.apiKey = secretKey;
        
        try {
            Session session = Session.retrieve(sessionId);
            Session expiredSession = session.expire();
            return mapSessionToDTO(expiredSession);
        } catch (StripeException e) {
            logger.error("Error expiring session: {}", e.getMessage());
            throw e;
        }
    }
}
