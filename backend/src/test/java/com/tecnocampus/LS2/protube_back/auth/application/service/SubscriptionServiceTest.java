package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.application.port.in.ProfileUseCase;
import com.tecnocampus.LS2.protube_back.auth.domain.port.in.ManageSubscriptionUseCase;
import com.tecnocampus.LS2.protube_back.auth.domain.port.out.SubscriptionPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {
    private final SubscriptionPort port = mock(SubscriptionPort.class);
    private final SubscriptionService service = new SubscriptionService(port);
    private final UUID subscriber = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();

    @Test
    void subscribesAndUnsubscribes() {
        when(port.targetExists(target)).thenReturn(true);

        assertTrue(service.subscribe(subscriber, target).subscribed());
        assertTrue(service.unsubscribe(subscriber, target).subscribed() == false);
        verify(port).add(subscriber, target);
        verify(port).remove(subscriber, target);
    }

    @Test
    void readsStatusAndListsProfiles() {
        when(port.targetExists(target)).thenReturn(true);
        when(port.exists(subscriber, target)).thenReturn(true);
        var profile = new ProfileUseCase.Profile(target, "creator", "Creator", null, null, null, null, true);
        when(port.findSubscriptions(subscriber)).thenReturn(List.of(profile));

        assertTrue(service.getStatus(subscriber, target).subscribed());
        assertEquals(List.of(profile), service.listSubscriptions(subscriber));
    }

    @Test
    void rejectsInvalidSubscriptionTargets() {
        when(port.targetExists(target)).thenReturn(false);
        assertThrows(ManageSubscriptionUseCase.SubscriptionTargetNotFoundException.class,
                () -> service.subscribe(subscriber, target));
        when(port.targetExists(subscriber)).thenReturn(true);
        assertThrows(ManageSubscriptionUseCase.SelfSubscriptionException.class,
                () -> service.subscribe(subscriber, subscriber));
        assertThrows(IllegalArgumentException.class, () -> service.listSubscriptions(null));
    }
}
