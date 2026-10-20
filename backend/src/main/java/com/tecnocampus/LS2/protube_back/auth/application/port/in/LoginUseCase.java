package com.tecnocampus.LS2.protube_back.auth.application.port.in;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;

public interface LoginUseCase {
    User authenticate(String email, String password);

    class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
}
