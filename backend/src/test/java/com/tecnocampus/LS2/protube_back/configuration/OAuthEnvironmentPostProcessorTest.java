package com.tecnocampus.LS2.protube_back.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OAuthEnvironmentPostProcessorTest {
    private final OAuthEnvironmentPostProcessor processor = new OAuthEnvironmentPostProcessor();

    @Test
    void emptyEnvironmentCredentialsUseLocalPlaceholders() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("ENV_PROTUBE_GOOGLE_CLIENT_ID", "")
                .withProperty("ENV_PROTUBE_GOOGLE_CLIENT_SECRET", "");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals("local-disabled-client",
                System.getProperty("spring.security.oauth2.client.registration.google.client-id"));
        assertEquals("local-disabled-secret",
                System.getProperty("spring.security.oauth2.client.registration.google.client-secret"));
    }

    @Test
    void configuredEnvironmentCredentialsArePreserved() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("ENV_PROTUBE_GOOGLE_CLIENT_ID", "client-id")
                .withProperty("ENV_PROTUBE_GOOGLE_CLIENT_SECRET", "client-secret");

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals("client-id",
                System.getProperty("spring.security.oauth2.client.registration.google.client-id"));
        assertEquals("client-secret",
                System.getProperty("spring.security.oauth2.client.registration.google.client-secret"));
    }
}
