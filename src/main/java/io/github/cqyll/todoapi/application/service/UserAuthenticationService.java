package io.github.cqyll.todoapi.application.service;

import io.github.cqyll.todoapi.application.port.inbound.UserAuthenticationUseCase;
import io.github.cqyll.todoapi.application.port.outbound.UserRepositoryPort;
import io.github.cqyll.todoapi.domain.User;
import io.github.cqyll.todoapi.application.port.outbound.PasswordHasherPort;
import io.github.cqyll.todoapi.application.port.outbound.TokenProviderPort;

public class UserAuthenticationService implements UserAuthenticationUseCase {
	private final UserRepositoryPort userRepository;
	private final PasswordHasherPort passwordHasher;
	private final TokenProviderPort tokenProvider;

	public UserAuthenticationService(
			UserRepositoryPort userRepository,
			PasswordHasherPort passwordHasher,
			TokenProviderPort tokenProvider) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
		this.tokenProvider = tokenProvider;
	}

	@Override
	public String login(String email, String rawPassword) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Invalid credentials: Email not found!"));

		// dummy user state check
		if (!user.isActive()) {
			throw new IllegalStateException("Account not active");
		}

		if (!user.verifyPassword(rawPassword, passwordHasher)) {
			throw new IllegalArgumentException("Invalid credentials: Incorrect Password!");
		}

		return tokenProvider.createToken(user.getId());
	}

	//    @Override
	//    public String login(LoginRequest request) {
	//    	
	//    	// this is a transport level validation check, it doesn't belong in here
	//        if (request == null || request.getEmail() == null || request.getPassword() == null) {
	//            throw new IllegalArgumentException("Email and password required");
	//        }
	//        
	//        var user = userRepository.findByEmail(request.getEmail())
	//            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
	//        
	//        // check user state
	//        if (!user.isActive()) {
	//            throw new IllegalStateException("Account is not active");
	//        }
	//        
	//        // verify password
	//        if (!user.verifyPassword(request.getPassword(), passwordHasher)) {
	//            throw new IllegalArgumentException("Invalid credentials");
	//        }
	//        
	//        // return session token
	//        return tokenProvider.createToken(user.getId());
	//    }
}