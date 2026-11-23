package com.tecnocampus.LS2.protube_back.auth.domain.port.in;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;

import java.util.List;
import java.util.UUID;

public interface ManageSubscriptionUseCase {
    SubscriptionStatus getStatus(UUID subscriberId, UUID subscribedToId);

    SubscriptionStatus subscribe(UUID subscriberId, UUID subscribedToId);

    SubscriptionStatus unsubscribe(UUID subscriberId, UUID subscribedToId);

    List<ProfileUseCase.Profile> listSubscriptions(UUID subscriberId);

    record SubscriptionStatus(UUID userId, boolean subscribed) {}

    class SubscriptionTargetNotFoundException extends RuntimeException {
        public SubscriptionTargetNotFoundException(UUID userId) {
            super("Subscription target not found: " + userId);
        }
    }

    class SelfSubscriptionException extends RuntimeException {
        public SelfSubscriptionException() {
            super("Users cannot subscribe to themselves.");
        }
    }
}
