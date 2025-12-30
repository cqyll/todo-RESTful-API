// auth.config.AppConfig.java
package auth.config;

import auth.adapter.inbound.web.UserController;
import auth.adapter.inbound.web.AuthController;
import auth.application.service.UserRegistrationService;
import auth.application.service.UserAuthenticationService;
import auth.adapter.outbound.persistence.InMemoryUserAdapter;
import auth.adapter.outbound.security.SimplePasswordHasherAdapter;
import auth.adapter.outbound.security.FakeTokenProviderAdapter;

public class AppConfig {
    private UserRegistrationService userRegistrationService;
    private UserAuthenticationService userAuthenticationService;
    private UserController userController;
    private AuthController authController;
    
    public AppConfig() {
        initialize();
    }
    
    private void initialize() {
        // created adapters
        var userRepository = new InMemoryUserAdapter();
        var passwordHasher = new SimplePasswordHasherAdapter();
        var tokenProvider = new FakeTokenProviderAdapter();
        
        // created services for each use case
        userRegistrationService = new UserRegistrationService(userRepository, passwordHasher);
        userAuthenticationService = new UserAuthenticationService(
            userRepository, passwordHasher, tokenProvider);
        
        // created corresponding controllers
        userController = new UserController(userRegistrationService);
        authController = new AuthController(userAuthenticationService);
    }
    
    public UserController getUserController() { return userController; }
    public AuthController getAuthController() { return authController; }
}