package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;
import com.tecnocampus.LS2.protube_back.auth.domain.port.in.ManageSubscriptionUseCase;
import com.tecnocampus.LS2.protube_back.auth.domain.port.out.SubscriptionPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService implements ManageSubscriptionUseCase {
    private final SubscriptionPort subscriptionPort;

    public SubscriptionService(SubscriptionPort subscriptionPort) {
        this.subscriptionPort = subscriptionPort;
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionStatus getStatus(UUID subscriberId, UUID subscribedToId) {
        requireUsers(subscriberId, subscribedToId);
        return new SubscriptionStatus(subscribedToId, subscriptionPort.exists(subscriberId, subscribedToId));
    }

    @Override
    @Transactional
    public SubscriptionStatus subscribe(UUID subscriberId, UUID subscribedToId) {
        requireUsers(subscriberId, subscribedToId);
        subscriptionPort.add(subscriberId, subscribedToId);
        return new SubscriptionStatus(subscribedToId, true);
    }

    @Override
    @Transactional
    public SubscriptionStatus unsubscribe(UUID subscriberId, UUID subscribedToId) {
        requireUsers(subscriberId, subscribedToId);
        subscriptionPort.remove(subscriberId, subscribedToId);
        return new SubscriptionStatus(subscribedToId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileUseCase.Profile> listSubscriptions(UUID subscriberId) {
        if (subscriberId == null) {
            throw new IllegalArgumentException("Authentication is required.");
        }
        return subscriptionPort.findSubscriptions(subscriberId);
    }

    private void requireUsers(UUID subscriberId, UUID subscribedToId) {
        if (subscriberId == null) {
            throw new IllegalArgumentException("Authentication is required.");
        }
        if (subscribedToId == null || !subscriptionPort.targetExists(subscribedToId)) {
            throw new SubscriptionTargetNotFoundException(subscribedToId);
        }
        if (subscriberId.equals(subscribedToId)) {
            throw new SelfSubscriptionException();
        }
    }
}
