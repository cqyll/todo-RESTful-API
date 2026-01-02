package io.github.cqyll.todoapi.application.port.inbound;


public interface UserRegistrationUseCase {
	String register(String email, String name, String rawPassword);
}
