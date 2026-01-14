package io.github.cqyll.todoapi.dto;

import java.util.Map;

public class BasicAuthRequest {
	private String email;
	private String password;

	public BasicAuthRequest() {
	}

	public BasicAuthRequest(String email, String password) {
		this.email = email;
		this.password = password;
	}
	
	public String getEmail() { return this.email; }
	public String getPassword() { return this.password; }

	
	public Map<String, String> toCredentials() {
		return Map.of(
				"email", email,
				"password", password);
	}
	
	
	 public void validate() {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
