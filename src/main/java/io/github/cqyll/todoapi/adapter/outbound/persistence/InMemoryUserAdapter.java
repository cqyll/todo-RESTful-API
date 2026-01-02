package io.github.cqyll.todoapi.adapter.outbound.persistence;

import io.github.cqyll.todoapi.application.port.outbound.UserRepositoryPort;
import io.github.cqyll.todoapi.domain.User;
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

	public Map<UUID,User> getUsersRepo() {
		return users;
	}
}