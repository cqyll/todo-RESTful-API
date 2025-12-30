package auth.adapter.outbound.security;

import java.util.UUID;

import auth.application.port.outbound.TokenProviderPort;

public class FakeTokenProviderAdapter implements TokenProviderPort {

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
