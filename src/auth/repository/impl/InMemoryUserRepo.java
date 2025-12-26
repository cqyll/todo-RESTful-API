package auth.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import auth.domain.User;
import auth.repository.UserRepository;

public class InMemoryUserRepo implements UserRepository{
	private final List<User> users = new ArrayList<User>();
	
	@Override
	public boolean existsByEmail(String email) {
		return users.stream()
				.anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
	}
	
	@Override
	public void save(User user) {
		users.add(user);
	}
	
	@Override
	public Optional<User> findByEmail(String email) {
		return users.stream()
				.filter(u -> u.getEmail().equalsIgnoreCase(email))
				.findFirst();
	}
}
