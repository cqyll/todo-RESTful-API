package io.github.cqyll.todoapi.application.port.inbound;


public interface UserAuthenticationUseCase {
	String login(String email, String rawPassword);
}
