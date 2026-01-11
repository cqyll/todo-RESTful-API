package io.github.cqyll.todoapi.application.service;

import io.github.cqyll.todoapi.domain.User;
import java.util.Map;

public class OAuthAuthenticationStrategy implements AuthenticationStrategy {
    private final BasicAuthenticationStrategy basicStrategy;
    
    public OAuthAuthenticationStrategy(BasicAuthenticationStrategy basicStrategy) {
        this.basicStrategy = basicStrategy;
    }
    
    @Override
    public String getName() {
        return "oauth";
    }
    
    @Override
    public User authenticate(Map<String, String> credentials) {
        String grantType = credentials.get("grant_type");
        
        switch (grantType) {
            case "password":
                // Convert OAuth params to basic auth params
                Map<String, String> basicCreds = Map.of(
                    "email", credentials.get("username"),
                    "password", credentials.get("password")
                );
                return basicStrategy.authenticate(basicCreds);
                
            // TODO: Implement other grant types
            default:
                throw new IllegalArgumentException("Grant type not implemented yet: " + grantType);
        }
    }
}