package io.github.cqyll.todoapi.application.port.outbound;

public interface PasswordHasherPort {
	String hash(String rawPassword);
	boolean matches(String rawPassword, String hashedPassword);
}
