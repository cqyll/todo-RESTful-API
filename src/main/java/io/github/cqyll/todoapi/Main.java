package io.github.cqyll.todoapi;


import io.github.cqyll.todoapi.application.port.outbound.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;

import io.github.cqyll.todoapi.adapter.outbound.persistence.InMemoryUserAdapter;
import io.github.cqyll.todoapi.adapter.outbound.security.SimplePasswordHasherAdapter;
import io.github.cqyll.todoapi.application.service.UserRegistrationService;
import io.github.cqyll.todoapi.config.AppConfig;
import io.github.cqyll.todoapi.domain.Password;
import io.github.cqyll.todoapi.adapter.outbound.security.FakeTokenProviderAdapter;

public class Main {
	
	public static void main(String[] args) throws Exception {
		ensureNotRoot();
	    AppConfig appConfig = new AppConfig();
	    HttpServer server = appConfig.createHttpServer();
	    server.start();

	    System.out.println("Server started on http://localhost:8080");

	    runRegistrationHttpTest();
	    runLoginHttpTest();

	    server.stop(0);
	}

	
	private static void runLoginHttpTest() throws Exception {

	    URL url = new URL("http://localhost:8080/login");
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
	    connection.setDoOutput(true);

	    String json = """
	            {
	                "email": "john@doe.com",
	                "password": "password"
	            }
	            """;

	    try (OutputStream os = connection.getOutputStream()) {
	        os.write(json.getBytes(StandardCharsets.UTF_8));
	    }

	    int status = connection.getResponseCode();
	    String responseBody = readAll(
	            status < 400 ? connection.getInputStream()
	                         : connection.getErrorStream()
	    );

	    if (status != 200) {
	        throw new RuntimeException("Expected HTTP 200 OK");
	    }

	    if (!responseBody.contains("TOKEN-")) {
	        throw new RuntimeException("Expected token in login response");
	    }

	    System.out.println("Login HTTP test passed");
	}

	
	private static void runRegistrationHttpTest() throws Exception {

	    URL url = new URL("http://localhost:8080/register");
	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
	    connection.setDoOutput(true);

	    String json = """
	            {
	                "name": "John Doe",
	                "email": "john@doe.com",
	                "password": "password"
	            }
	            """;

	    try (OutputStream os = connection.getOutputStream()) {
	        os.write(json.getBytes(StandardCharsets.UTF_8));
	    }

	    int status = connection.getResponseCode();
	    String responseBody = readAll(
	            status < 400 ? connection.getInputStream()
	                         : connection.getErrorStream()
	    );

	    if (status != 201) {
	        throw new RuntimeException("Expected HTTP 201 Created");
	    }

	    if (!responseBody.contains("TOKEN-")) {
	        throw new RuntimeException("Expected token in response");
	    }

	    System.out.println("Registration HTTP test passed");
	}

	
	private static String readAll(InputStream is) throws IOException {
		try (BufferedReader br = new BufferedReader(
										new InputStreamReader(is, StandardCharsets.UTF_8))) {
			StringBuilder sb = new StringBuilder();
			String line;
			
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			
			return sb.toString();
		}
	}
	
	private static void runServiceTests() {
		System.out.println("Service Tests");
		
		
		UserRepositoryPort userRepository = new InMemoryUserAdapter();
		PasswordHasherPort hasher = new SimplePasswordHasherAdapter();
		TokenProviderPort tokenProvider = new FakeTokenProviderAdapter();
		
		UserRegistrationService userRegistrationService = 
				new UserRegistrationService(userRepository, hasher, tokenProvider);

		String id = userRegistrationService.register(
				"test@example.com",
				"Test User",
				"securePassword123");
				
		
		System.out.println("Created user ID:" + id);
		
		try {
			userRegistrationService.register(
					"test@example.com",
					"Other user",
					"anotherPassword");
			 System.out.println("ERROR: duplicate email allowed");
		} catch(IllegalArgumentException e) {
			System.out.println("Expected error: " + e.getMessage());
		}
        
	}
	
	private static void runDomainTests() {

	    System.out.println("Domain Tests");

	    try {
	        Password.fromHash("short");
	        System.out.println("ERROR: invalid password hash allowed");
	    } catch (IllegalArgumentException e) {
	        System.out.println("Expected failure: " + e.getMessage());
	    }

	    Password valid = Password.fromHash(
	        "0123456789abcdef0123456789abcdef"
	    );
	    System.out.println("Valid password created");
	}
	
	private static void ensureNotRoot() {
		String user = System.getProperty("user.name");
		if ("root".equals(user)) {
			throw new IllegalStateException("Refusing to run as root. Run as a normal user.");
		}
	}

}