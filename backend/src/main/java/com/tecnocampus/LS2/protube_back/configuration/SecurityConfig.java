package com.tecnocampus.LS2.protube_back.configuration;

import com.tecnocampus.LS2.protube_back.auth.application.service.GoogleOAuthService;
import com.tecnocampus.LS2.protube_back.auth.infrastructure.adapter.out.persistence.JdbcGoogleAccountAdapter.GoogleAccountConflictException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Value;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {
    @Value("${pro_tube.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                             AuthenticationSuccessHandler oauthSuccessHandler,
                                             AuthenticationFailureHandler oauthFailureHandler) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/**", "/login/**", "/api/**", "/media/**", "/").permitAll()
                        .anyRequest().permitAll())
                .oauth2Login(oauth -> oauth
                        .successHandler(oauthSuccessHandler)
                        .failureHandler(oauthFailureHandler));
        return http.build();
    }

    @Bean
    AuthenticationSuccessHandler oauthSuccessHandler(GoogleOAuthService service) {
        return (request, response, authentication) -> {
            try {
                OAuth2User principal = (OAuth2User) authentication.getPrincipal();
                var user = service.authenticate(principal.getAttribute("sub"), principal.getAttribute("email"),
                        principal.getAttribute("name"));
                request.getSession(true).setAttribute("authenticatedUserId", user.getId());
                response.sendRedirect(frontendUrl + "/#oauth-success");
            } catch (RuntimeException exception) {
                response.sendRedirect(frontendUrl + "/#login?oauthError=" +
                        URLEncoder.encode(errorMessage(exception), StandardCharsets.UTF_8));
            }
        };
    }

    @Bean
    AuthenticationFailureHandler oauthFailureHandler() {
        return (request, response, exception) -> response.sendRedirect(
                frontendUrl + "/#login?oauthError=" +
                        URLEncoder.encode(errorMessage(exception), StandardCharsets.UTF_8));
    }

    private String errorMessage(Exception exception) {
        Throwable cause = exception.getCause() instanceof GoogleAccountConflictException
                ? exception.getCause() : exception;
        return cause.getMessage() == null ? "Google sign-in failed." : cause.getMessage();
    }
}
