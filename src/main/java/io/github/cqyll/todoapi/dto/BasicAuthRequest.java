package io.github.cqyll.todoapi.dto;

import java.util.Map;

public class BasicAuthRequest extends AuthRequest {
	private String email;
	private String password;
	
	public BasicAuthRequest() {
		super("basic");
	}
	
	@Override
	public Map<String, String> toCredentials() {
		return Map.of(
				"email", email,
				"password", password);
	}
	
	@Override
	public void validate() {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		// password required for basic auth flows (including OAuth 2.0 password grants)
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Password is required");
		}
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}
}
