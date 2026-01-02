package io.github.cqyll.todoapi.config;

import io.github.cqyll.todoapi.adapter.inbound.web.UserController;
import io.github.cqyll.todoapi.adapter.inbound.web.AuthController;
import io.github.cqyll.todoapi.application.service.UserRegistrationService;
import io.github.cqyll.todoapi.application.service.UserAuthenticationService;
import io.github.cqyll.todoapi.adapter.outbound.persistence.InMemoryUserAdapter;
import io.github.cqyll.todoapi.adapter.outbound.security.SimplePasswordHasherAdapter;
import io.github.cqyll.todoapi.adapter.outbound.security.FakeTokenProviderAdapter;

public class AppConfig {
    private UserRegistrationService userRegistrationService;
    private UserAuthenticationService userAuthenticationService;
    private UserController userController;
    private AuthController authController;
    
    public AppConfig() {
        initialize();
    }
    
    private void initialize() {
        // created adapters
        var userRepository = new InMemoryUserAdapter();
        var passwordHasher = new SimplePasswordHasherAdapter();
        var tokenProvider = new FakeTokenProviderAdapter();
        
        // created services for each use case
        userRegistrationService = new UserRegistrationService(userRepository, passwordHasher);
        userAuthenticationService = new UserAuthenticationService(
            userRepository, passwordHasher, tokenProvider);
        
        // created corresponding controllers
        userController = new UserController(userRegistrationService);
        authController = new AuthController(userAuthenticationService);
    }
    
    public UserController getUserController() { return userController; }
    public AuthController getAuthController() { return authController; }
}