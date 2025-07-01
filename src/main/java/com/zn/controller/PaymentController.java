package com.zn.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.zn.dto.CheckoutRequest;
import com.zn.dto.ResponceDTO;
import com.zn.service.StripeService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<ResponceDTO> createCheckoutSession(@RequestBody CheckoutRequest request) {
        log.info("Received request to create checkout session: {}", request);
        try {
            // Get complete session details from service
            Session session = stripeService.createDetailedCheckoutSession(request);
            
            // Use service method to map session to DTO with proper timestamp conversion
            ResponceDTO response = stripeService.mapSessionToResponceDTO(session);
            
            log.info("Checkout session created successfully. Session ID: {}", session.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating checkout session: {}", e.getMessage(), e);
            ResponceDTO errorResponse = new ResponceDTO();
            errorResponse.setStatus("error");
            errorResponse.setPayment_status("failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponceDTO> getCheckoutSession(@PathVariable String id) {
        log.info("Retrieving checkout session with ID: {}", id);
        try {
            ResponceDTO responseDTO = stripeService.retrieveSession(id);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            log.error("Error retrieving checkout session: {}", e.getMessage(), e);
            ResponceDTO errorResponse = new ResponceDTO();
            errorResponse.setStatus("error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    
    @PostMapping("/{id}/expire")
    public ResponseEntity<ResponceDTO> expireSession(@PathVariable String id) {
        log.info("Expiring checkout session with ID: {}", id);
        try {
            ResponceDTO responseDTO = stripeService.expireSession(id);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            log.error("Error expiring checkout session: {}", e.getMessage(), e);
            ResponceDTO errorResponse = new ResponceDTO();
            errorResponse.setStatus("error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) throws IOException {
        log.info("Received webhook request");
        String payload;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            payload = reader.lines().collect(Collectors.joining("\n"));
        }
        
        String sigHeader = request.getHeader("Stripe-Signature");
        
        try {
            Event event = stripeService.constructWebhookEvent(payload, sigHeader);
            stripeService.processWebhookEvent(event);
            return ResponseEntity.ok().body("Webhook processed successfully");
        } catch (SignatureVerificationException e) {
            log.error("⚠️ Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature verification failed");
        }
    }
}
