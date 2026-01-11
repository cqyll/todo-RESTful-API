package io.github.cqyll.todoapi.application.service;

import java.util.Map;

import io.github.cqyll.todoapi.application.port.inbound.LoginUseCase;
import io.github.cqyll.todoapi.domain.User;
import io.github.cqyll.todoapi.application.port.outbound.TokenProviderPort;

public class LoginService implements LoginUseCase {
	private final Map<String, AuthenticationStrategy> strategies;
	private final TokenProviderPort tokenProvider;

	public LoginService(
			Map<String, AuthenticationStrategy> strategies,
			TokenProviderPort tokenProvider) {
		this.strategies = strategies;
		this.tokenProvider = tokenProvider;
	}

	@Override
	public String login(Map<String, String> credentials) {
		String strategyName = credentials.get("strategy");
		if (strategyName == null) {
			throw new IllegalArgumentException("Authentication strategy must be specified field!");
		}
		
		AuthenticationStrategy strategy = strategies.get(strategyName);
		if (strategy == null) {
			throw new IllegalArgumentException("Unsupported authentication stategy: " + strategyName);
		}
		
		User user = strategy.authenticate(credentials);
		
		if (!user.isActive()) {
			throw new IllegalStateException("Account not active");
		}
		
		return tokenProvider.createToken(user.getId());
	}
}