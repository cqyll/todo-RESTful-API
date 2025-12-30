package auth.application.port.inbound;

import auth.dto.LoginRequest;

public interface UserAuthenticationUseCase {
	String login(LoginRequest request);
}
