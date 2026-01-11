package io.github.cqyll.todoapi.adapter.inbound.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.github.cqyll.todoapi.application.port.inbound.LoginUseCase;
import io.github.cqyll.todoapi.dto.AuthRequest;
import io.github.cqyll.todoapi.dto.BasicAuthRequest;
import io.github.cqyll.todoapi.dto.OAuthTokenRequest;

public class AuthController implements HttpHandler {
    private final LoginUseCase loginService;
    private static final ObjectMapper mapper = new ObjectMapper();

    public AuthController(LoginUseCase loginService) {
        this.loginService = loginService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try {
            // Determine content type to decide which DTO to use
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            
            AuthRequest authRequest;
            if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                // Parse as OAuth request
                authRequest = parseOAuthRequest(exchange);
            } else {
                // Parse as Basic auth request (JSON)
                authRequest = parseBasicAuthRequest(exchange);
            }
            
            // Validate the request
            authRequest.validate();
            
            // Convert to credentials map and add strategy
            Map<String, String> credentials = new HashMap<>(authRequest.toCredentials());
            credentials.put("strategy", authRequest.getStrategy());
            
            // Call service
            String token = loginService.login(credentials);
            
            // Send response
            sendResponse(exchange, 200, createSuccessResponse(authRequest, token));
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 500, Map.of("error", "Internal server error"));
        }
    }

    private AuthRequest parseBasicAuthRequest(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return mapper.readValue(is, BasicAuthRequest.class);
        }
    }

    private AuthRequest parseOAuthRequest(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> formParams = parseFormUrlEncoded(body);
        
        OAuthTokenRequest request = new OAuthTokenRequest();
        request.setGrantType(formParams.get("grant_type"));
        request.setClientId(formParams.get("client_id"));
        request.setClientSecret(formParams.get("client_secret"));
        request.setUsername(formParams.get("username"));
        request.setPassword(formParams.get("password"));
        request.setRefreshToken(formParams.get("refresh_token"));
        request.setCode(formParams.get("code"));
        request.setRedirectUri(formParams.get("redirect_uri"));
        request.setScope(formParams.get("scope"));
        
        return request;
    }

    private Map<String, String> parseFormUrlEncoded(String body) {
        Map<String, String> params = new HashMap<>();
        for (String param : body.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                params.put(
                    URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                );
            }
        }
        return params;
    }

    private Map<String, Object> createSuccessResponse(AuthRequest request, String token) {
        if ("oauth".equals(request.getStrategy())) {
            return Map.of(
                "access_token", token,
                "token_type", "Bearer",
                "expires_in", 3600
            );
        } else {
            return Map.of("token", token);
        }
    }

    private void sendResponse(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}