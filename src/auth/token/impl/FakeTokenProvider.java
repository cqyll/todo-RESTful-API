package auth.token.impl;

import java.util.UUID;

import auth.token.TokenProvider;

public class FakeTokenProvider implements TokenProvider {
	
	@Override
	public String createToken(UUID userId) {
		return "TOKEN-" + userId.toString();
	}
	
	@Override
	public boolean validateToken(String token) {
		return token != null && token.startsWith("TOKEN-");
	}
	
	@Override
	public UUID extractUserId(String token) {
		if (!validateToken(token)) {
			throw new IllegalArgumentException("Invalid token!");
		}
		return UUID.fromString(token.substring(6));
	}
}
