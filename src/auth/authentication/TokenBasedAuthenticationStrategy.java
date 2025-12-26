package auth.authentication;

import auth.domain.User;
import auth.token.TokenProvider;

public class TokenBasedAuthenticationStrategy implements AuthenticationStrategy {
	private final TokenProvider tokenProvider;
	
	public TokenBasedAuthenticationStrategy(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}
	
	@Override
	public String generateToken(User user) {
		return tokenProvider.createToken(user.getId());
	}
}
