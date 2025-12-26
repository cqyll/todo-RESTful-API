package auth.service;

import java.util.UUID;

import auth.domain.User;
import auth.dto.RegisterRequest;
import auth.repository.UserRepository;
import auth.security.PasswordHasher;
import auth.authentication.AuthenticationStrategy;

public class UserService {
	/*
	 * For proper Dependency Inversion field static types should be interface-based types
	 */
	
	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;
	private final AuthenticationStrategy authenticationStrategy;
	
	
	/**
	 * Constructor demonstrates Dependency Injection and Dependency Inversion.
	 * 
	 * DESIGN DECISION: All dependencies are injected as interfaces, not concrete classes.
	 * - Follows SOLID principle (D) 
	 * - Allows for runtime selection of implementations
	 * 
	 * @param userRepository Data persistence abstraction
	 * @param passWordHasher Password hashing abstraction (interface)
	 * @param authenticationStrategy Token generation abstraction
	 */
	public UserService(
			UserRepository userRepository,
			PasswordHasher passwordHasher,
			AuthenticationStrategy authenticationStrategy
	) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
		this.authenticationStrategy = authenticationStrategy;
	}
	
	public String register(RegisterRequest request) {
		validate(request);
		
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("Email already registered");
		}
		
		String passwordHash = passwordHasher.hash(request.getPassword());
		
		User user = new User(
				UUID.randomUUID(),
				request.getName(),
				request.getEmail(),
				passwordHash);
		
		userRepository.save(user);
		
		return authenticationStrategy.generateToken(user);			
	
	}
	
	private void validate(RegisterRequest request) {
		
		if(request == null) {
			throw new IllegalArgumentException("Request cannot be null");
		}
		
		if(isBlank(request.getName())) {
			throw new IllegalArgumentException("Name is required");
		}
		
		if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }

        if (isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Password is required");
        }
	}
	
	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
	
}
