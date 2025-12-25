package auth.repository;

import java.util.Optional;
import auth.domain.User;

public interface UserRepository {
	boolean existsByEmail(String email);
	void save(User user);
	
	Optional<User> findByEmail(String email); // prevents null propagation
}
