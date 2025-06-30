package com.zn.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.billingportal.Session;
import com.stripe.net.Webhook;
import com.zn.dto.CheckoutRequest;
import com.zn.service.StripeService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
	@Autowired
    private  StripeService stripeService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody CheckoutRequest request) {
        log.info("Received request to create checkout session: {}", request);
        try {
            String sessionUrl = stripeService.createCheckoutSession(request);
            log.info("Checkout session created successfully. URL: {}", sessionUrl);
            return ResponseEntity.ok(sessionUrl);
        } catch (Exception e) {
            log.error("Error creating checkout session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
        
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) throws IOException {
        log.info("Received webhook request");
        String payload = new BufferedReader(new InputStreamReader(request.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        log.debug("Webhook payload: {}", payload);

        String sigHeader = request.getHeader("Stripe-Signature");
        log.debug("Stripe-Signature header: {}", sigHeader);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            log.info("Webhook event constructed successfully. Event type: {}", event.getType());
        } catch (SignatureVerificationException e) {
            log.error("Webhook error: Invalid signature", e);
            return ResponseEntity.badRequest().body("⚠️ Webhook error: Invalid signature.");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            if (dataObjectDeserializer.getObject().isPresent()) {
                Session session = (Session) dataObjectDeserializer.getObject().get();
                log.info("✅ Payment successful for session: {}", session.getId());
                log.info("💳 Customer email: {}", session.getCustomer());

                log.info("💰 Amount total: {}");
            } else {
                log.warn("Event data object deserialization failed ");
            }
        }

        return ResponseEntity.ok("✅ Webhook received");
    }
}
