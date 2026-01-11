package io.github.cqyll.todoapi.application.port.inbound;

import java.util.Map;

public interface LoginUseCase {
	// String login(String email, String rawPassword);
	String login(Map<String, String> credentials);
}
