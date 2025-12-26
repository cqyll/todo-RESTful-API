package auth.token;

import java.util.UUID;

public interface TokenProvider {
	
	String createToken(UUID userId);
	boolean validateToken(String token);
	UUID extractUserId(String token);
}
