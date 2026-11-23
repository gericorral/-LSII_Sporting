package com.tecnocampus.LS2.protube_back.auth.domain.port.out;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;

import java.util.List;
import java.util.UUID;

public interface SubscriptionPort {
    boolean targetExists(UUID userId);

    boolean exists(UUID subscriberId, UUID subscribedToId);

    void add(UUID subscriberId, UUID subscribedToId);

    void remove(UUID subscriberId, UUID subscribedToId);

    List<ProfileUseCase.Profile> findSubscriptions(UUID subscriberId);
}
