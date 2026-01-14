package io.github.cqyll.todoapi.application.service;

import java.util.Map;

import io.github.cqyll.todoapi.application.port.outbound.UserRepositoryPort;
import io.github.cqyll.todoapi.domain.User;
import io.github.cqyll.todoapi.application.port.outbound.PasswordHasherPort;

public class BasicAuthenticationStrategy implements AuthenticationStrategy {
	private final UserRepositoryPort userRepo;
	private final PasswordHasherPort hasher;
	
	public BasicAuthenticationStrategy(
			UserRepositoryPort userRepo, PasswordHasherPort hasher) {
		this.hasher = hasher;
		this.userRepo = userRepo;
	}
	
	@Override
	public User authenticate(Map<String, String> credentials) {
		String email = credentials.get("email");
		String rawPassword = credentials.get("password");
		User user = this.userRepo.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Invalid credentials: Email not found!"));
		
		if (!user.verifyPassword(rawPassword, hasher)) {
			throw new IllegalArgumentException("Invalid credentials: Incorrect Password!");
		}
		
		return user;
	}
	
	@Override
	public String getName() {
		return "basic";
	}
}
