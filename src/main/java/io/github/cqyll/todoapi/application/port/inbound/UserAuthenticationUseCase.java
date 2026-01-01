package io.github.cqyll.todoapi.application.port.inbound;

import io.github.cqyll.todoapi.dto.LoginRequest;

public interface UserAuthenticationUseCase {
	String login(LoginRequest request);
}
