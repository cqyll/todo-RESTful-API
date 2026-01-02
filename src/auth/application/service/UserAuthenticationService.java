package auth.application.service;

import auth.application.port.inbound.UserAuthenticationUseCase;
import auth.application.port.outbound.UserRepositoryPort;
import auth.application.port.outbound.PasswordHasherPort;
import auth.application.port.outbound.TokenProviderPort;
import auth.dto.LoginRequest;

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
    public String login(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password required");
        }
        
        var user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        
        // check user state
        if (!user.isActive()) {
            throw new IllegalStateException("Account is not active");
        }
        
        // verify password
        if (!user.verifyPassword(request.getPassword(), passwordHasher)) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        // return session token
        return tokenProvider.createToken(user.getId());
    }
}