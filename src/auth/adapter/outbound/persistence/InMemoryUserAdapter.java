package auth.adapter.outbound.persistence;

import auth.application.port.outbound.UserRepositoryPort;
import auth.domain.User;
import java.util.*;

public class InMemoryUserAdapter implements UserRepositoryPort {
    private final Map<UUID, User> users = new HashMap<>();
    private final Map<String, UUID> emailIndex = new HashMap<>();
    
    @Override
    public void save(User user) {
        users.put(user.getId(), user);
        emailIndex.put(user.getEmail().toLowerCase(), user.getId());
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(emailIndex.get(email.toLowerCase()))
            .map(users::get);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return emailIndex.containsKey(email.toLowerCase());
    }
    
    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }
}