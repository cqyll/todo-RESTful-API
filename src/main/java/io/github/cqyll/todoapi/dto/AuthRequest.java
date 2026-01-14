package io.github.cqyll.todoapi.dto;

import java.util.Map;

public abstract class AuthRequest {
	private final String strategy;
	
	public abstract Map<String, String> toCredentials();
	public abstract void validate();
	
	protected AuthRequest(String strat) {
		this.strategy = strat;
	}
	public String getStrategy() { return this.strategy; }
}
