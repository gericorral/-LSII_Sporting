package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.auth.domain.port.in.ManageSubscriptionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SubscriptionController {
    private final ManageSubscriptionUseCase subscriptionUseCase;
    private final SessionPort sessionPort;

    public SubscriptionController(ManageSubscriptionUseCase subscriptionUseCase, SessionPort sessionPort) {
        this.subscriptionUseCase = subscriptionUseCase;
        this.sessionPort = sessionPort;
    }

    @GetMapping("/profiles/{userId}/subscription")
    public ResponseEntity<?> status(@PathVariable UUID userId) {
        UUID subscriberId = sessionPort.currentUserId();
        if (subscriberId == null) return unauthorized("Log in to manage subscriptions.");
        try {
            return ResponseEntity.ok(subscriptionUseCase.getStatus(subscriberId, userId));
        } catch (ManageSubscriptionUseCase.SubscriptionTargetNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (ManageSubscriptionUseCase.SelfSubscriptionException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    @PutMapping("/profiles/{userId}/subscription")
    public ResponseEntity<?> subscribe(@PathVariable UUID userId) {
        return change(userId, true);
    }

    @DeleteMapping("/profiles/{userId}/subscription")
    public ResponseEntity<?> unsubscribe(@PathVariable UUID userId) {
        return change(userId, false);
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> list() {
        UUID subscriberId = sessionPort.currentUserId();
        if (subscriberId == null) return unauthorized("Log in to view your subscriptions.");
        return ResponseEntity.ok(subscriptionUseCase.listSubscriptions(subscriberId));
    }

    private ResponseEntity<?> change(UUID userId, boolean subscribe) {
        UUID subscriberId = sessionPort.currentUserId();
        if (subscriberId == null) return unauthorized("Log in to manage subscriptions.");
        try {
            return ResponseEntity.ok(subscribe
                    ? subscriptionUseCase.subscribe(subscriberId, userId)
                    : subscriptionUseCase.unsubscribe(subscriberId, userId));
        } catch (ManageSubscriptionUseCase.SubscriptionTargetNotFoundException exception) {
            return ResponseEntity.notFound().build();
        } catch (ManageSubscriptionUseCase.SelfSubscriptionException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }

    private ResponseEntity<Map<String, String>> unauthorized(String message) {
        return ResponseEntity.status(401).body(Map.of("message", message));
    }
}
