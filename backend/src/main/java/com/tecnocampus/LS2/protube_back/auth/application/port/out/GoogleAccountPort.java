package com.tecnocampus.LS2.protube_back.auth.application.port.out;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;

public interface GoogleAccountPort {
    User provisionOrFind(String subject, String email, String displayName);
}
