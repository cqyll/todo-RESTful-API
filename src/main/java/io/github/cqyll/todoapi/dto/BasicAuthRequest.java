package io.github.cqyll.todoapi.dto;

import java.util.Map;

public class BasicAuthRequest extends AuthRequest {
	private String email;
	private String password;

	public BasicAuthRequest() {
		super("basic");
	}

	public BasicAuthRequest(String email, String password) {
		super("basic");
		this.email = email;
		this.password = password;
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
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
