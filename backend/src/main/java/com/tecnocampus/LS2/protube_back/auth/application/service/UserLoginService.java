package com.tecnocampus.LS2.protube_back.auth.application.service;

import com.tecnocampus.LS2.protube_back.auth.domain.model.User;
import com.tecnocampus.LS2.protube_back.auth.application.port.in.LoginUseCase;
import com.tecnocampus.LS2.protube_back.auth.application.port.out.UserAuthenticationPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;

@Service
public class UserLoginService implements LoginUseCase {
    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private final UserAuthenticationPort userRepository;

    public UserLoginService(UserAuthenticationPort userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public User authenticate(String email, String password) {
        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .filter(user -> verifyPassword(password, user.getPasswordHash()))
                .orElseThrow(() -> new LoginUseCase.InvalidCredentialsException(INVALID_CREDENTIALS));
    }

    private boolean verifyPassword(String password, String encodedHash) {
        try {
            String[] parts = encodedHash.split("\\$", -1);
            if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            PBEKeySpec keySpec = new PBEKeySpec(
                    password.toCharArray(), salt, iterations, expectedHash.length * 8);
            byte[] actualHash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec).getEncoded();
            return MessageDigest.isEqual(actualHash, expectedHash);
        } catch (RuntimeException | java.security.GeneralSecurityException exception) {
            return false;
        }
    }

}
