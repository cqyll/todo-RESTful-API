package io.github.cqyll.todoapi.dto;

import java.util.Map;

public abstract class AuthRequest {
	private final String strategy;
	
	protected AuthRequest(String strategy) {
		this.strategy = strategy;
	}
	
	public String getStrategy() {
		return this.strategy;
	}
	
	public abstract Map<String, String> toCredentials();
	
	public abstract void validate(); //check req fields present
}
