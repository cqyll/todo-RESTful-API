package io.github.cqyll.todoapi.adapter.inbound.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.github.cqyll.todoapi.application.service.UserRegistrationService;
import io.github.cqyll.todoapi.dto.RegisterRequest;

public class UserController implements HttpHandler {
	
	private final UserRegistrationService registrationService;
	private static final ObjectMapper mapper = new ObjectMapper();
	
	
	public UserController(UserRegistrationService registrationService) {
		this.registrationService = registrationService;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		// only allow POST request
		if (!"POST".equals(exchange.getRequestMethod())) {
			// request not supported; no response body length
			exchange.sendResponseHeaders(405, -1); 
			return;
		}
		
		
		// parse JSON using jackson
		
		RegisterRequest request; // dto
		
		try (InputStream is = exchange.getRequestBody()) {
			request = mapper.readValue(is, RegisterRequest.class);
		} catch (Exception e) {
			sendResponse(exchange, 400, "Invalid JSON");
			return;
		}
		
		/*
		 * transport-level validation (very basic)
		 * will have to refactor on OAuth integration -- password
		 */
		
		if (request.getEmail() == null || request.getName() == null || request.getPassword() == null) {
			sendResponse(exchange, 400, "Missing fields");
			return;
		}
		
		// call service
	
		try {
			String userId = registrationService.register(
					request.getEmail(),
					request.getName(),
					request.getPassword());
			
			
			// success
			sendResponse(exchange, 201, userId);
		} catch (IllegalArgumentException e) {
			sendResponse(exchange, 409, e.getMessage());
		}
		
		
		
	}
	
	private void sendResponse(HttpExchange exchange, int status, Object body) 
			throws IOException {
		
		byte[] bytes = mapper.writeValueAsBytes(body);
		
		exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
		exchange.sendResponseHeaders(status, bytes.length);
		
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}
}
