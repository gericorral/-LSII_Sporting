package com.tecnocampus.LS2.protube_back.auth.application.port.in;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;

public interface RegistrationUseCase {
    User register(String email, String username, String password);

    class DuplicateUserException extends RuntimeException {
        private final String field;

        public DuplicateUserException(String field, String message) {
            super(message);
            this.field = field;
        }

        public DuplicateUserException(String field, String message, Throwable cause) {
            super(message, cause);
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }
}
