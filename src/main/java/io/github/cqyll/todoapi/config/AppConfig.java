package io.github.cqyll.todoapi.config;

import io.github.cqyll.todoapi.adapter.inbound.web.UserController;
import io.github.cqyll.todoapi.adapter.inbound.web.AuthController;
import io.github.cqyll.todoapi.application.service.UserRegistrationService;
import io.github.cqyll.todoapi.application.service.LoginService;
import io.github.cqyll.todoapi.application.service.BasicAuthenticationStrategy;
import io.github.cqyll.todoapi.application.service.OAuthAuthenticationStrategy;
import io.github.cqyll.todoapi.application.service.AuthenticationStrategy;
import io.github.cqyll.todoapi.application.port.outbound.UserRepositoryPort;
import io.github.cqyll.todoapi.application.port.inbound.LoginUseCase;
import io.github.cqyll.todoapi.application.port.inbound.UserRegistrationUseCase;
import io.github.cqyll.todoapi.application.port.outbound.PasswordHasherPort;
import io.github.cqyll.todoapi.application.port.outbound.TokenProviderPort;
import io.github.cqyll.todoapi.adapter.outbound.persistence.InMemoryUserAdapter;
import io.github.cqyll.todoapi.adapter.outbound.security.SimplePasswordHasherAdapter;
import io.github.cqyll.todoapi.adapter.outbound.security.FakeTokenProviderAdapter;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class AppConfig {
    private UserRegistrationUseCase userRegistrationService;
    private LoginUseCase loginService;
    private UserController userController;
    private AuthController authController;

    public AppConfig() {
        initialize();
    }

    private void initialize() {
        // Create adapters
        UserRepositoryPort userRepository = new InMemoryUserAdapter();
        PasswordHasherPort passwordHasher = new SimplePasswordHasherAdapter();
        TokenProviderPort tokenProvider = new FakeTokenProviderAdapter();

        // Create authentication strategies
        BasicAuthenticationStrategy basicStrategy = new BasicAuthenticationStrategy(
            userRepository, passwordHasher
        );
        
        OAuthAuthenticationStrategy oauthStrategy = new OAuthAuthenticationStrategy(
            basicStrategy
        );
        
        // Create strategy map
        Map<String, AuthenticationStrategy> strategies = new HashMap<>();
        strategies.put("basic", basicStrategy);
        strategies.put("oauth", oauthStrategy);

        // Create services for each use case
        userRegistrationService = new UserRegistrationService(
            userRepository, passwordHasher, tokenProvider
        );
        
        loginService = new LoginService(
            strategies, tokenProvider
        );

        // Create corresponding controllers
        userController = new UserController(userRegistrationService);
        authController = new AuthController(loginService); // Single auth controller for all auth flows
    }

    public HttpServer createHttpServer() {
        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress("localhost", 8080), 0);
            
            registerContexts(server);
            return server;
        } catch(IOException e) {
            throw new RuntimeException("Failed to create HTTP server", e);
        }
    }

    public UserController getUserController() { return userController; }
    public AuthController getAuthController() { return authController; }
    
    private void registerContexts(HttpServer server) {
        server.createContext("/register", userController);
        server.createContext("/login", authController); // Basic auth endpoint
        server.createContext("/oauth/token", authController); // OAuth token endpoint
        // Both endpoints use the same AuthController - it detects content type
    }
}