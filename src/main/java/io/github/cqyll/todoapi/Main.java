package io.github.cqyll.todoapi;

import io.github.cqyll.todoapi.adapter.outbound.persistence.InMemoryUserAdapter;
import io.github.cqyll.todoapi.adapter.outbound.security.SimplePasswordHasherAdapter;
import io.github.cqyll.todoapi.application.service.UserRegistrationService;
import io.github.cqyll.todoapi.dto.RegisterRequest;
import io.github.cqyll.todoapi.adapter.outbound.security.FakeTokenProviderAdapter;

public class Main {
	
	public static void main(String[] args) {
		
		var userRepository = new InMemoryUserAdapter();
		var passwordHasher = new SimplePasswordHasherAdapter();
		var tokenProvider = new FakeTokenProviderAdapter();
		
		
		UserRegistrationService userRegistrationService = 
				new UserRegistrationService(userRepository, passwordHasher);

		
		System.out.println("=== TEST 1: Register multiple users ===");
		
		RegisterRequest user1 = new RegisterRequest();
        user1.setName("John Doe");
        user1.setEmail("john@doe.com");
        user1.setPassword("password123");
        
        RegisterRequest user2 = new RegisterRequest();
        user2.setName("Jane Smith");
        user2.setEmail("jane@smith.com");
        user2.setPassword("secret456");
        
        
        System.out.println("Email being registered: " + user1.getEmail());
        String first = userRegistrationService.register(user1);
        String second = userRegistrationService.register(user2);
        
        System.out.println("First user Id:" + first);
        System.out.println("Second user Id:" + second);
        System.out.println(userRepository.getUsersRepo().toString());
        
        System.out.println("Attempting to re-add user2...");
        String third = userRegistrationService.register(user2);
	}
	

}
	

