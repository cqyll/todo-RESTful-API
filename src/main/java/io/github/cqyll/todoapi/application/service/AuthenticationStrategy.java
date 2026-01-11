package io.github.cqyll.todoapi.application.service;

import java.util.Map;

import io.github.cqyll.todoapi.domain.User;

public interface AuthenticationStrategy {
	String getName();
	User authenticate(Map<String,String> credentials);
}
