package auth.application.service;

import auth.application.port.inbound.UserRegistrationUseCase;
import auth.application.port.outbound.UserRepositoryPort;
import auth.application.port.outbound.PasswordHasherPort;
import auth.domain.User;
import auth.domain.Password;
import auth.dto.RegisterRequest;

public class UserRegistrationService implements UserRegistrationUseCase {
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    
    public UserRegistrationService(
            UserRepositoryPort userRepository,
            PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }
    
    @Override
    public String register(RegisterRequest request) {
        validate(request);
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        String hash = passwordHasher.hash(request.getPassword());
        Password password = Password.fromHash(hash);
        
        // create user entity obj with pw VO
        User user = User.createWithPassword(
            request.getEmail(),
            request.getName(),
            password
        );
        
        userRepository.save(user);
        
        return user.getId().toString();
    }
    
    private void validate(RegisterRequest request) {
        if (request == null) throw new IllegalArgumentException("Request cannot be null");
        if (isBlank(request.getName())) throw new IllegalArgumentException("Name is required");
        if (isBlank(request.getEmail())) throw new IllegalArgumentException("Email is required");
        if (isBlank(request.getPassword())) throw new IllegalArgumentException("Password is required");
    }
    
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}