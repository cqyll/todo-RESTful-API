package auth.application.port.inbound;

import auth.dto.RegisterRequest;

public interface UserRegistrationUseCase {
	String register(RegisterRequest request);
}
