package com.tecnocampus.LS2.protube_back.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

public class OAuthEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final String LOCAL_CLIENT_ID = "local-disabled-client";
    private static final String LOCAL_CLIENT_SECRET = "local-disabled-secret";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String clientId = environment.getProperty("ENV_PROTUBE_GOOGLE_CLIENT_ID");
        String clientSecret = environment.getProperty("ENV_PROTUBE_GOOGLE_CLIENT_SECRET");

        System.setProperty(
                "spring.security.oauth2.client.registration.google.client-id",
                StringUtils.hasText(clientId) ? clientId : LOCAL_CLIENT_ID);
        System.setProperty(
                "spring.security.oauth2.client.registration.google.client-secret",
                StringUtils.hasText(clientSecret) ? clientSecret : LOCAL_CLIENT_SECRET);
    }
}
