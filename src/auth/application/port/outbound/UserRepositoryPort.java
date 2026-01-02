package auth.application.port.outbound;

import auth.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
	
	void save(User user);
	boolean existsByEmail(String email);
	Optional<User> findByEmail(String email);
	Optional<User> findById(UUID id);
}
