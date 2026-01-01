package io.github.cqyll.todoapi.adapter.inbound.web;

import io.github.cqyll.todoapi.application.service.UserRegistrationService;
import io.github.cqyll.todoapi.dto.RegisterRequest;
import io.github.cqyll.todoapi.dto.RegisterResponse;

public class UserController {

    private final UserRegistrationService registrationService;

    public UserController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public RegisterResponse register(RegisterRequest request) {
        String userId = registrationService.register(request);

        return new RegisterResponse(userId, "User registered successfully!");
    }
}
