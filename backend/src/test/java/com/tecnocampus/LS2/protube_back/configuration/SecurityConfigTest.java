package com.tecnocampus.LS2.protube_back.configuration;

import com.tecnocampus.LS2.protube_back.auth.application.service.GoogleOAuthService;
import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence.JdbcGoogleAccountAdapter.GoogleAccountConflictException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {
    @Test
    void successfulGoogleAuthenticationCreatesSessionAndRedirects() throws Exception {
        GoogleOAuthService service = mock(GoogleOAuthService.class);
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "frontendUrl", "http://localhost");
        User user = new User(UUID.randomUUID(), "google@example.com", "google_user", null);
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("sub")).thenReturn("subject");
        when(principal.getAttribute("email")).thenReturn("google@example.com");
        when(principal.getAttribute("name")).thenReturn("Google User");
        when(service.authenticate("subject", "google@example.com", "Google User")).thenReturn(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();
        config.oauthSuccessHandler(service).onAuthenticationSuccess(request, response, authentication);

        assertEquals(user.getId(), request.getSession(false).getAttribute("authenticatedUserId"));
        assertEquals("http://localhost/#oauth-success", response.getRedirectedUrl());
    }

    @Test
    void oauthFailureEscapesMessageAndRedirectsToLogin() throws Exception {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "frontendUrl", "http://localhost");
        MockHttpServletResponse response = new MockHttpServletResponse();

        config.oauthFailureHandler().onAuthenticationFailure(null, response,
                new OAuth2AuthenticationException(new OAuth2Error("server_error"), "Google unavailable"));

        assertEquals("http://localhost/#login?oauthError=Google+unavailable", response.getRedirectedUrl());
    }

    @Test
    void successfulHandlerRedirectsProvisioningErrorsWithoutAuthenticating() throws Exception {
        GoogleOAuthService service = mock(GoogleOAuthService.class);
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "frontendUrl", "http://localhost");
        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("sub")).thenReturn("subject");
        when(principal.getAttribute("email")).thenReturn("local@example.com");
        when(principal.getAttribute("name")).thenReturn("Local User");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(service.authenticate(any(), any(), any())).thenThrow(new GoogleAccountConflictException());

        MockHttpServletResponse response = new MockHttpServletResponse();
        config.oauthSuccessHandler(service).onAuthenticationSuccess(
                new MockHttpServletRequest(), response, authentication);

        assertTrue(response.getRedirectedUrl().contains("local+account"));
    }

    @Test
    void oauthFailureUsesFallbackForMissingMessage() throws Exception {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "frontendUrl", "http://localhost");
        MockHttpServletResponse response = new MockHttpServletResponse();

        config.oauthFailureHandler().onAuthenticationFailure(null, response,
                new OAuth2AuthenticationException(new OAuth2Error("server_error")));

        assertEquals("http://localhost/#login?oauthError=Google+sign-in+failed.", response.getRedirectedUrl());
    }
}
