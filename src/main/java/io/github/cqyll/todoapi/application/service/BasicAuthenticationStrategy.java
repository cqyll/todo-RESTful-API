package io.github.cqyll.todoapi.application.service;

import java.util.Map;

import io.github.cqyll.todoapi.application.port.outbound.PasswordHasherPort;
import io.github.cqyll.todoapi.application.port.outbound.UserRepositoryPort;
import io.github.cqyll.todoapi.domain.User;

public class BasicAuthenticationStrategy implements AuthenticationStrategy {
	private final UserRepositoryPort userRepository;
	private final PasswordHasherPort hasher;
	
	public BasicAuthenticationStrategy(
			UserRepositoryPort userRepo, PasswordHasherPort hasher) {
		this.hasher = hasher;
		this.userRepository = userRepo;
	}
	
	@Override
	public User authenticate(Map<String,String> creds) {
		String email = creds.get("email");
		String rawPassword = creds.get("password");
		User user = this.userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Invalid credentials: Email Not Found!"));
		
		
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
