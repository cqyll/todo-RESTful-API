package auth.authentication;

import auth.domain.User;

public interface AuthenticationStrategy {
	
	String generateToken(User user);
}
