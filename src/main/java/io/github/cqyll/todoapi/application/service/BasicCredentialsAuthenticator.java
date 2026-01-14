package io.github.cqyll.todoapi.application.service;

import java.util.Map;

import io.github.cqyll.todoapi.application.port.outbound.PasswordHasherPort;
import io.github.cqyll.todoapi.application.port.outbound.UserRepositoryPort;
import io.github.cqyll.todoapi.domain.User;

public class BasicCredentialsAuthenticator {
    private final UserRepositoryPort userRepo;
    private final PasswordHasherPort hasher;

    public BasicCredentialsAuthenticator(UserRepositoryPort userRepo, PasswordHasherPort hasher) {
        this.hasher = hasher;
        this.userRepo = userRepo;
    }

    public User authenticate(Map<String, String> credentials) {
        String email = credentials.get("email");
        String rawPassword = credentials.get("password");

        User user = this.userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.verifyPassword(rawPassword, hasher)) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return user;
    }
}
