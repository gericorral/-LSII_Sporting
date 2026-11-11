package com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.in.web;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.SessionPort;
import com.tecnocampus.LS2.protube_back.auth.domain.port.in.ManageSubscriptionUseCase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SubscriptionControllerTest {
    private final ManageSubscriptionUseCase useCase = mock(ManageSubscriptionUseCase.class);
    private final SessionPort session = mock(SessionPort.class);
    private final SubscriptionController controller = new SubscriptionController(useCase, session);
    private final UUID subscriber = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();

    @Test
    void requiresAuthentication() {
        when(session.currentUserId()).thenReturn(null);
        assertEquals(401, controller.list().getStatusCode().value());
        assertEquals(401, controller.subscribe(target).getStatusCode().value());
    }

    @Test
    void managesSubscriptionAndMapsErrors() {
        when(session.currentUserId()).thenReturn(subscriber);
        when(useCase.subscribe(subscriber, target))
                .thenReturn(new ManageSubscriptionUseCase.SubscriptionStatus(target, true));
        when(useCase.unsubscribe(subscriber, target))
                .thenReturn(new ManageSubscriptionUseCase.SubscriptionStatus(target, false));
        assertEquals(200, controller.subscribe(target).getStatusCode().value());
        assertEquals(200, controller.unsubscribe(target).getStatusCode().value());
        reset(useCase);
        doThrow(new ManageSubscriptionUseCase.SubscriptionTargetNotFoundException(target))
                .when(useCase).subscribe(subscriber, target);
        assertEquals(404, controller.subscribe(target).getStatusCode().value());
        reset(useCase);
        doThrow(new ManageSubscriptionUseCase.SelfSubscriptionException())
                .when(useCase).subscribe(subscriber, target);
        assertEquals(400, controller.subscribe(target).getStatusCode().value());
    }
}
