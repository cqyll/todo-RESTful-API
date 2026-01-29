package io.github.cqyll.todoapi.application.service;

import io.github.cqyll.todoapi.application.port.inbound.BasicLoginUseCase;
import io.github.cqyll.todoapi.application.port.outbound.TokenProviderPort;
import io.github.cqyll.todoapi.domain.User;

import java.util.Map;

public class BasicLoginService implements BasicLoginUseCase {
    private final BasicCredentialsAuthenticator basicAuth;
    private final TokenProviderPort tokenProvider;

    public BasicLoginService(BasicCredentialsAuthenticator basicAuth, TokenProviderPort tokenProvider) {
        this.basicAuth = basicAuth;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public String login(String email, String password) {
        User user = basicAuth.authenticate(Map.of("email", email, "password", password));

        if (!user.isActive()) {
            throw new IllegalStateException("Account not active");
        }
        return tokenProvider.createToken(user.getId());
    }
}
