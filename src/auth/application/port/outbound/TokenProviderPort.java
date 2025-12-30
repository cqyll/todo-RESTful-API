package auth.application.port.outbound;

import java.util.UUID;

public interface TokenProviderPort {
	String createToken(UUID userId);
	boolean validateToken(String token);
	UUID extractUserId(String token);
}
