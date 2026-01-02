package io.github.cqyll.todoapi.application.port.outbound;

import io.github.cqyll.todoapi.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
	
	void save(User user);
	boolean existsByEmail(String email);
	Optional<User> findByEmail(String email);
	Optional<User> findById(UUID id);
}
