package io.github.cqyll.todoapi.application.port.inbound;

import io.github.cqyll.todoapi.dto.RegisterRequest;

public interface UserRegistrationUseCase {
	String register(RegisterRequest request);
}
