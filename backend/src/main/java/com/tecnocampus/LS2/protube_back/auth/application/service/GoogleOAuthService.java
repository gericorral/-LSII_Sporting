package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.application.port.out.GoogleAccountPort;
import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleOAuthService {
    private final GoogleAccountPort accounts;

    public GoogleOAuthService(GoogleAccountPort accounts) {
        this.accounts = accounts;
    }

    @Transactional
    public User authenticate(String subject, String email, String displayName) {
        if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google did not provide a usable identity");
        }
        return accounts.provisionOrFind(subject, email, displayName);
    }
}
