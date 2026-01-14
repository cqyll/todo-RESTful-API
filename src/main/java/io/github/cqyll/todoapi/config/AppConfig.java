package io.github.cqyll.todoapi.config;

import com.sun.net.httpserver.HttpServer;
import io.github.cqyll.todoapi.adapter.inbound.web.LoginController;
import io.github.cqyll.todoapi.adapter.inbound.web.OAuthTokenController;
import io.github.cqyll.todoapi.adapter.inbound.web.UserController;
import io.github.cqyll.todoapi.adapter.outbound.persistence.InMemoryUserAdapter;
import io.github.cqyll.todoapi.adapter.outbound.security.FakeTokenProviderAdapter;
import io.github.cqyll.todoapi.adapter.outbound.security.SimplePasswordHasherAdapter;
import io.github.cqyll.todoapi.application.port.outbound.PasswordHasherPort;
import io.github.cqyll.todoapi.application.port.outbound.TokenProviderPort;
import io.github.cqyll.todoapi.application.port.outbound.UserRepositoryPort;
import io.github.cqyll.todoapi.application.service.BasicCredentialsAuthenticator;
import io.github.cqyll.todoapi.application.service.BasicLoginService;
import io.github.cqyll.todoapi.application.service.OAuthTokenService;
import io.github.cqyll.todoapi.application.service.UserRegistrationService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class AppConfig {
    private UserController userController;
    private LoginController loginController;
    private OAuthTokenController oauthTokenController;

    public AppConfig() { initialize(); }

    private void initialize() {
        UserRepositoryPort userRepo = new InMemoryUserAdapter();
        PasswordHasherPort hasher = new SimplePasswordHasherAdapter();
        TokenProviderPort tokenProvider = new FakeTokenProviderAdapter();

        UserRegistrationService reg = new UserRegistrationService(userRepo, hasher, tokenProvider);
        userController = new UserController(reg);

        BasicCredentialsAuthenticator basicAuth = new BasicCredentialsAuthenticator(userRepo, hasher);

        loginController = new LoginController(new BasicLoginService(basicAuth, tokenProvider));

        oauthTokenController = new OAuthTokenController(
                new OAuthTokenService(basicAuth, tokenProvider, "todo-web", "todo-secret")
        );
    }

    public HttpServer createHttpServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
            server.createContext("/register", userController);
            server.createContext("/login", loginController);
            server.createContext("/oauth/token", oauthTokenController);
            return server;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
