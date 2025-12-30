package auth;

import auth.authentication.AuthenticationStrategy;
import auth.authentication.TokenBasedAuthenticationStrategy;
import auth.dto.RegisterRequest;
import auth.repository.UserRepository;
import auth.repository.impl.InMemoryUserRepo;
import auth.security.PasswordHasher;
import auth.security.SimpleHasher;
import auth.service.UserService;
import auth.token.TokenProvider;
import auth.token.impl.FakeTokenProvider;

public class Main {
	
	public static void main(String[] args) {
		
		UserRepository userRepository = new InMemoryUserRepo();
		PasswordHasher passwordHasher = new SimpleHasher();
		TokenProvider tokenProvider = new FakeTokenProvider();
		AuthenticationStrategy authStrategy = 
				new TokenBasedAuthenticationStrategy(tokenProvider);
		
		UserService userService = new UserService(
				userRepository,
				passwordHasher,
				authStrategy);
		
		System.out.println("=== TEST 1: Register multiple users ===");
        testMultipleUsers(userService);

        System.out.println("\n=== TEST 2: Duplicate email registration ===");
        testDuplicateUser(userService);
		
		
//		RegisterRequest request = new RegisterRequest();
//		request.setName("John Doe");
//		request.setEmail("john@doe.com");
//		request.setPassword("password");
//		
//		String token = userService.register(request);
//		
//		System.out.println("Registration successful");
//		System.out.println("Token: " + token);
//			
	
	}
	
	private static void testMultipleUsers(UserService userService) {

        RegisterRequest user1 = new RegisterRequest();
        user1.setName("John Doe");
        user1.setEmail("john@doe.com");
        user1.setPassword("password123");

        RegisterRequest user2 = new RegisterRequest();
        user2.setName("Jane Smith");
        user2.setEmail("jane@smith.com");
        user2.setPassword("secret456");

        String token1 = userService.register(user1);
        String token2 = userService.register(user2);

        System.out.println("User 1 token: " + token1);
        System.out.println("User 2 token: " + token2);

        assertNotEqual(token1, token2, "Tokens should be unique");
    }
	
	 private static void testDuplicateUser(UserService userService) {

	        RegisterRequest duplicate = new RegisterRequest();
	        duplicate.setName("Johnny Clone");
	        duplicate.setEmail("john@doe.com"); // already registered
	        duplicate.setPassword("anotherPassword");

	        try {
	            userService.register(duplicate);
	            System.out.println("ERROR: Duplicate user was allowed");
	        } catch (IllegalArgumentException e) {
	            System.out.println("Expected exception caught: " + e.getMessage());
	        }
	    }
		
	 private static void assertNotEqual(String a, String b, String message) {
	        if (a.equals(b)) {
	            throw new AssertionError(message);
	        }
	    }	
}
	

